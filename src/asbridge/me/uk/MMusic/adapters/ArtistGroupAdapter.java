package asbridge.me.uk.MMusic.adapters;

import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import asbridge.me.uk.MMusic.R;
import asbridge.me.uk.MMusic.classes.ArtistGroup;


/**
 * Created by AsbridgeD on 21/12/2015.
 */
public class ArtistGroupAdapter extends BaseExpandableListAdapter {

    private final static String TAG = "ArtistGroupAdapter";

    private final SparseArray<ArtistGroup> groups;
    public LayoutInflater inflater;
    public Activity activity;

    public ArtistGroupAdapter(Activity act, SparseArray<ArtistGroup> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
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
        CheckedTextView text = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artistrow_details, null);
        }
        text = (CheckedTextView) convertView.findViewById(R.id.textView1);
        text.setText(checkedSong.song.getTitle());
        text.setChecked(checkedSong.selected);
        text.setOnClickListener(new OnSongClickListener(groupPosition,childPosition));

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
            } else {
                checkedSong.selected = true;
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

        CheckedTextView ctv = (CheckedTextView) convertView.findViewById(R.id.textView1);
        ctv.setText(group.artistName);
        ctv.setChecked(isExpanded);

        ImageButton btnAgSelectNone = (ImageButton) convertView.findViewById(R.id.btnAgSelectNone);
        btnAgSelectNone.setOnClickListener(new OnArtistClickListener(groupPosition));
        switch (selectedState) {
            case 0:
                btnAgSelectNone.setImageResource(R.drawable.ic_selectionbox_none);
                break;
            case 1:
                btnAgSelectNone.setImageResource(R.drawable.ic_selectionbox_some);
                break;
            case 2:
                btnAgSelectNone.setImageResource(R.drawable.ic_selectionbox_all);
                break;
        }
        return convertView;
    }

    class OnArtistClickListener implements View.OnClickListener {

        int groupPosition;

        // constructor
        public OnArtistClickListener(int groupPosition) {
            this.groupPosition = groupPosition;
        }
        @Override
        public void onClick(View v) {
            Log.d(TAG,"artistremoveclicked");
            // checkbox clicked
            final ArtistGroup artistGroup = (ArtistGroup) getGroup(groupPosition);
            int selectedState = artistGroup.getSelectedState();
            if (selectedState == 2)
                artistGroup.doSelectNone();
            else
                artistGroup.doSelectAll();

            notifyDataSetChanged();
        }
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
