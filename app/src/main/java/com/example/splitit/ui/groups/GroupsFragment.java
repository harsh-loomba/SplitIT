package com.example.splitit.ui.groups;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitit.GroupActivity;
import com.example.splitit.MainActivity;
import com.example.splitit.R;
import com.example.splitit.model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class GroupsFragment extends Fragment {

    private GroupsViewModel mViewModel;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference groupsDatabase;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }
    private FloatingActionButton btn_add_group;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View groupsView = inflater.inflate(R.layout.fragment_groups,container,false);

        btn_add_group = groupsView.findViewById(R.id.btn_add_group);
        btn_add_group.setClickable(true);
        recyclerView = groupsView.findViewById(R.id.group_recycler);


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        String uid = firebaseUser.getUid();

        groupsDatabase = FirebaseDatabase.getInstance().getReference().child(uid);


        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        LoadData();

        addData();

        return groupsView;
    }

    private void LoadData() {

        Query query = FirebaseDatabase.getInstance().getReference().child(firebaseAuth.getUid());
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(query, new SnapshotParser<Data>() {
                    @NonNull
                    @Override
                    public Data parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new Data(
                                snapshot.child("groupName").getValue().toString(),
                                null,
                                null);
                    }
                })
                .build();

        FirebaseRecyclerAdapter <Data,GroupsViewHolder> adapter = new FirebaseRecyclerAdapter<>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupsViewHolder holder, int position, @NonNull Data model) {
                holder.setGroupName(model.getGroupName());

                holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, GroupActivity.class);
                        intent.putExtra("intentGroupName", model.getGroupName());
                        context.startActivity(intent);
                    }
                });
            }

            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_recycler_data, parent, false));
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView parentLayout;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            parentLayout = itemView.findViewById(R.id.group_name_recycler);
        }

        private void setGroupName(String group_Name){
            TextView mGroupName = mView.findViewById(R.id.group_name_recycler);
            mGroupName.setText(group_Name);
        }
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        mViewModel = new ViewModelProvider(this).get(GroupsViewModel.class);
//        // TODO: Use the ViewModel
//    }

    private void addData(){
        btn_add_group.setOnClickListener(v -> groupInsert());
    }

    public void groupInsert(){

        AlertDialog.Builder groupDiag = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View addGroupView = inflater.inflate(R.layout.add_group_layout,null);
        groupDiag.setView(addGroupView);
        AlertDialog dialog = groupDiag.create();

        EditText groupNameInput = addGroupView.findViewById(R.id.group_name);
        Button btnSave = addGroupView.findViewById(R.id.btn_save_group);
        Button btnCancel = addGroupView.findViewById(R.id.btn_cancel_save_group);

        btnSave.setOnClickListener(v -> {
            String group_name = groupNameInput.getText().toString().trim();

            if(TextUtils.isEmpty(group_name)){
                groupNameInput.setError("Required Field...");
                return;
            }

            Data data = new Data(group_name, null, null
            );
            groupsDatabase.child(group_name).setValue(data);

            Toast.makeText(getActivity(), "Group Added", Toast.LENGTH_SHORT).show();

            dialog.dismiss();

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


}