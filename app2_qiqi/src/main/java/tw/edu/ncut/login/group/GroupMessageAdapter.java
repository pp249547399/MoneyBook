package tw.edu.ncut.login.group;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tw.edu.ncut.login.myapplication.R;

/**
 * Created by us on 2018/3/21.
 */

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder>{
    //------------------------------------------------------------------------------
    private List<String> rDataSet = new ArrayList<>();
    private int SentMessage = 0, ReceviceMessage = 1, current = 2;
    private String sender[] = null;
    public GroupMessageAdapter(List<String> data){
        this.rDataSet = data;

    }

    @Override
    public int getItemViewType(int position) {
        sender = rDataSet.get(position).split(",");
        //Log.d("dfjdkjflks",sender[1]);
        if (sender[0].equals("ReceviceMessage")) {
            return ReceviceMessage;
        } else if (sender[0].equals("SentMessage")){
            return SentMessage;
        }else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == ReceviceMessage) {
            current = ReceviceMessage;
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_received, parent, false);
        } else {
            current = SentMessage;
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_sent, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        sender = rDataSet.get(position).split(",");
        if (holder.group_tv_mMessage!= null && current == ReceviceMessage) {
            holder.group_tv_mMessage.setText(sender[1]);
            holder.group_tv_mName.setText(sender[2]);
            holder.group_tv_mTime.setText(sender[3]);
        } else if (holder.group_tv_uMessage != null && current == SentMessage){
            holder.group_tv_uMessage.setText(sender[1]);
            holder.group_tv_uName.setText(sender[2]);
            holder.group_tv_uTime.setText(sender[3]);
        }

    }

    @Override
    public int getItemCount() {
        return rDataSet.size();
    }
    //
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView group_tv_mMessage;
        public TextView group_tv_uMessage;
        public TextView group_tv_mName;
        public TextView group_tv_uName;
        public TextView group_tv_mTime;
        public TextView group_tv_uTime;
        //
        public ViewHolder(View itemView) {
            super(itemView);
            group_tv_mMessage = itemView.findViewById(R.id.group_tv_mMessage);
            group_tv_uMessage = itemView.findViewById(R.id.group_tv_uMess);
            group_tv_mName = itemView.findViewById(R.id.group_tv_mName);
            group_tv_uName = itemView.findViewById(R.id.group_tv_uName);
            group_tv_mTime = itemView.findViewById(R.id.group_tv_mTime);
            group_tv_uTime = itemView.findViewById(R.id.group_tv_uTime);
        }

    }
}
