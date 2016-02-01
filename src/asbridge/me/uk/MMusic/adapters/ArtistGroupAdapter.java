package asbridge.me.uk.MMusic.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.ArtistGroup;
import asbridge.me.uk.MMusic.classes.Song;
import asbridge.me.uk.MMusic.controls.TriStateButton;
import asbridge.me.uk.MMusic.utils.MusicContent;

import java.util.ArrayList;


/**
 * Created by AsbridgeD on 21/12/2015.
 */
public class ArtistGroupAdapter extends BaseExpandableListAdapter {

    private final static String TAG = "ArtistGroupAdapter";

    private final SparseArray<ArtistGroup> groups;
    private int selectionState;
    public LayoutInflater inflater;
    public Activity activity;

    public interface OnSelectionStateChangedListener {
        void onSelectionStateChanged (int selectionState);
    }

    private OnSelectionStateChangedListener listener = null;

    public void setOnSelectionStateChangedListener(OnSelectionStateChangedListener l) {
        listener = l;
    }

    public ArtistGroupAdapter(Activity act, SparseArray<ArtistGroup> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
    }

    // override notifyDatasetchanged to recalculate the selection state
    // and fire event if listener is set and the state has changed
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        int oldState = selectionState;
        int numSongs = 0;
        int numSelected = 0;
        for(int i = 0; i < groups.size(); i++) {
            int key = groups.keyAt(i);
            // get the object by the key.
            ArtistGroup ag = groups.get(key);
            numSongs += ag.getNumSongs();
            numSelected += ag.getNumSelected();
        }

        if (numSelected == 0)
            selectionState = 0;
        else if (numSelected == numSongs)
            selectionState = 2;
        else
            selectionState = 1;

        if (listener != null && selectionState != oldState) {
            listener.onSelectionStateChanged(selectionState);
        }
    }

    public int getSelectionState() {
        return selectionState;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).songs.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final ArtistGroup.SelectedSong checkedSong = (ArtistGroup.SelectedSong) getChild(groupPosition, childPosition);
        TextView text = null;
        CheckBox checkbox = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artistrow_details, null);
        }
        text = (TextView) convertView.findViewById(R.id.textView1);
        text.setText(checkedSong.song.getTitle());

        checkbox = (CheckBox) convertView.findViewById(R.id.songcheckbox);
        checkbox.setChecked(checkedSong.selected);
        checkbox.setOnClickListener(new OnSongClickListener(groupPosition,childPosition));
        if (childPosition % 2 == 1) {
            convertView.setBackgroundColor(Color.rgb(0x22,0x22,0x22));
        } else {
            convertView.setBackgroundColor(Color.rgb(0x44,0x44,0x44));
        }
        return convertView;
    }

    class OnSongClickListener implements View.OnClickListener {
        int groupPosition;
        int childPosition;
        // constructor
        public OnSongClickListener(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }
        @Override
        public void onClick(View v) {
            // checkbox clicked
            final ArtistGroup.SelectedSong checkedSong = (ArtistGroup.SelectedSong) getChild(groupPosition, childPosition);
            if (checkedSong.selected) {
                checkedSong.selected = false;
                MusicContent.removeSongFromCurrentPlaylist(activity, checkedSong.song);
            } else {
                checkedSong.selected = true;
                MusicContent.addSongToCurrentPlaylist(activity, checkedSong.song);
            }

            notifyDataSetChanged();
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).songs.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artistrow_group, null);
        }
        ArtistGroup group = (ArtistGroup) getGroup(groupPosition);

        int selectedState = group.getSelectedState();

        TextView ctv = (TextView) convertView.findViewById(R.id.textView1);
        ctv.setText(group.artistName);

        TriStateButton btnAgSelect = (TriStateButton) convertView.findViewById(R.id.btnAgSelect);
        btnAgSelect.setState(selectedState);
        btnAgSelect.setOnClickListener(new OnAgSelectClickListener(groupPosition) );
        return convertView;
    }

    // call from the 'select all' or 'select none' buttons
    public void selectAllorNone(boolean newState) {
        // loop round the groups
        ArrayList<Song> songsToAdd = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            ArtistGroup ag = (ArtistGroup) getGroup(i);
            if (newState == true) { // We are selecting the songs
                for (ArtistGroup.SelectedSong ss : ag.songs) {
                    if (!ss.selected) {
                        ss.selected = true;
                        // add the selected song to a list to add to the DB
                        songsToAdd.add(ss.song);
                    }
                }
            } else {
                // when clearing the songs, here we just clear them in the adapter and later update DB in one go
                for (ArtistGroup.SelectedSong ss : ag.songs) {
                    if (ss.selected) {
                        ss.selected = false;
                    }
                }
            }
        }
        if (newState == false) {
            // removing songs from DB in one go
            MusicContent.removeAllSongsFromCurrentPlaylist(activity);
        } else {
            // removing songs from DB in one go
            MusicContent.addSongsToCurrentPlaylist(activity, songsToAdd);
        }

        notifyDataSetChanged();
    }

    // when group checkbox is clicked.
    // select or unselct all the child songs
    class OnAgSelectClickListener implements View.OnClickListener {
        int groupPosition;
        // constructor
        public OnAgSelectClickListener(int groupPosition) {
            this.groupPosition = groupPosition;
        }

        @Override
        public void onClick(View v)
        {
            final ArtistGroup artistGroup = (ArtistGroup) getGroup(groupPosition);
            int groupState = artistGroup.getSelectedState();
            if (groupState == 2) { /* all currently selected */
                clearAllSongsInGroup(artistGroup);
            } else {
                selectAllSongsInGroup(artistGroup);
            }
            notifyDataSetChanged();
        }
    }

    private void clearAllSongsInGroup(ArtistGroup ag) {
        // remember a list of songs in the group, so we can delete them from DB in one go
        ArrayList<Song> songsToRemove = new ArrayList<>();
        for (ArtistGroup.SelectedSong ss : ag.songs) {
            if (ss.selected) {
                ss.selected = false;
                songsToRemove.add(ss.song);
            }
        }
        MusicContent.removeSongsFromCurrentPlaylist(activity, songsToRemove);
    }

    private void selectAllSongsInGroup(ArtistGroup ag) {
        // remember a list of songs in the group, so we can add them to DB in one go
        ArrayList<Song> songsToAdd = new ArrayList<>();
        for (ArtistGroup.SelectedSong ss : ag.songs) {
            if (!ss.selected) {
                ss.selected = true;
                songsToAdd.add(ss.song);
            }
        }
        MusicContent.addSongsToCurrentPlaylist(activity, songsToAdd);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
