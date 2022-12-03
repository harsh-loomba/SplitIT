package com.example.splitit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitit.model.Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference groupsDatabase;
    private membersAdapter adapter;


    private Button btn_add_member;
    private Button btn_settle_debts;

    private List<String> member_names = new ArrayList<>();
    private List<Float> amount_paid = new ArrayList<>();
    private String group_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        group_name = getIncomingIntent();
        getSupportActionBar().setTitle(group_name);

        btn_add_member = findViewById(R.id.btn_add_member);
        btn_settle_debts = findViewById(R.id.btn_settle_debts);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        groupsDatabase = FirebaseDatabase.getInstance().getReference().child(firebaseUser.getUid()).child(group_name);

        RecyclerView recyclerView = this.findViewById(R.id.member_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new membersAdapter(this, member_names,amount_paid);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        settleDebts();
        addData();
        LoadData();
    }

    private void LoadData() {

        groupsDatabase.child("memberNames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    member_names.clear();
                    for(DataSnapshot dss : snapshot.getChildren()){
                        String member_name = dss.getValue(String.class);
                        member_names.add(member_name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        groupsDatabase.child("amountPaid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    amount_paid.clear();
                    for(DataSnapshot dss : snapshot.getChildren()){
                        Float amt = dss.getValue(Float.class);
                        amount_paid.add(amt);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class membersAdapter extends RecyclerView.Adapter<MembersViewHolder>{

        private final Context context;


        private final List<String>Member_names;
        private final List<Float>Amount_paid;

        public membersAdapter(Context context, List<String> member_names, List<Float> amount_paid) {
            this.context = context;
            Member_names = member_names;
            Amount_paid = amount_paid;
        }

        @NonNull
        @Override
        public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MembersViewHolder(LayoutInflater.from(context).inflate(R.layout.member_recycler_data, parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
            holder.setGroupData(Member_names.get(position), Amount_paid.get(position));

            holder.relativeLayout.setOnClickListener(v -> setAmount(holder.getAbsoluteAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return Member_names.size();
        }
    }

    public static class MembersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView mMemberName;
        TextView mAmountPaid;
        RelativeLayout relativeLayout;

        public MembersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mMemberName = itemView.findViewById(R.id.member_name_recycler);
            mAmountPaid = itemView.findViewById(R.id.amount_paid_recycler);
            relativeLayout = itemView.findViewById(R.id.card_member_recycler);
        }

        private void setGroupData(String member_Name, Float amount_Paid){
            TextView myMemberName = mView.findViewById(R.id.member_name_recycler);
            TextView myAmountPaid = mView.findViewById(R.id.amount_paid_recycler);
            myMemberName.setText(member_Name);
            myAmountPaid.setText(String.valueOf(amount_Paid));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private String getIncomingIntent(){

        if(getIntent().hasExtra("intentGroupName")){
            return getIntent().getStringExtra("intentGroupName");
        }

        return "Split IT";
    }

    private void addData(){
        btn_add_member.setOnClickListener(v -> groupInsert());
    }

    private void settleDebts(){
        btn_settle_debts.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, SettleDebtsActivity.class);
            intent.putExtra("intentGroupName", group_name);
            context.startActivity(intent);
        });
    }

    public void setAmount(int position){
        AlertDialog.Builder amountDiag = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View setAmtView = inflater.inflate(R.layout.set_amount_paid,null);
        amountDiag.setView(setAmtView);

        TextView title = setAmtView.findViewById(R.id.group_layout_title);
        title.setText("Set Amount paid by " + member_names.get(position));

        AlertDialog dialog = amountDiag.create();

        EditText amtInput = setAmtView.findViewById(R.id.amount);
        Button btnSave = setAmtView.findViewById(R.id.btn_save_group);
        Button btnCancel = setAmtView.findViewById(R.id.btn_cancel_save_group);

        btnSave.setOnClickListener(v -> {
            Float amount = Float.valueOf(amtInput.getText().toString().trim());

            if(TextUtils.isEmpty(amtInput.getText().toString().trim())){
                amtInput.setError("Required Field...");
                return;
            }

            groupsDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    groupsDatabase.removeEventListener(this);
                    member_names = new ArrayList<>();
                    amount_paid = new ArrayList<>();

                    if(snapshot.child("memberNames").getValue() != null)
                    {
                        if (snapshot.child("memberNames").getValue() instanceof List)
                        {
                            member_names = (List<String>) snapshot.child("memberNames").getValue();
                            amount_paid = (List<Float>) snapshot.child("amountPaid").getValue();
                            assert amount_paid != null;
                            amount_paid.set(position,amount);
                        }
                    }

                    Data data = new Data(group_name,member_names,amount_paid);

                    groupsDatabase.setValue(data);
                    adapter.notifyDataSetChanged();
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });

            Toast.makeText(this, "Amount Updated", Toast.LENGTH_SHORT).show();

            dialog.dismiss();

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();


    }

    public void groupInsert(){

        AlertDialog.Builder memberDiag = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View addMemberView = inflater.inflate(R.layout.add_group_layout,null);
        memberDiag.setView(addMemberView);

        TextView title = addMemberView.findViewById(R.id.group_layout_title);
        title.setText("Add Member");

        AlertDialog dialog = memberDiag.create();

        EditText groupMemberInput = addMemberView.findViewById(R.id.group_name);
        Button btnSave = addMemberView.findViewById(R.id.btn_save_group);
        Button btnCancel = addMemberView.findViewById(R.id.btn_cancel_save_group);

        btnSave.setOnClickListener(v -> {
            String member_name = groupMemberInput.getText().toString().trim();

            if(TextUtils.isEmpty(member_name)){
                groupMemberInput.setError("Required Field...");
                return;
            }

            groupsDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    groupsDatabase.removeEventListener(this);
                    member_names = new ArrayList<>();
                    amount_paid = new ArrayList<>();

                    if(snapshot.child("memberNames").getValue()==null){
                        member_names.add(member_name);
                        amount_paid.add((float) 0);
                    }
                    else{
                        if (snapshot.child("memberNames").getValue() instanceof List) {
                            member_names = (List<String>) snapshot.child("memberNames").getValue();
                            amount_paid = (List<Float>) snapshot.child("amountPaid").getValue();
                            member_names.add(member_name);
                            amount_paid.add((float) 0);
                        }
                    }

                    Data data = new Data(group_name,member_names,amount_paid);

                    groupsDatabase.setValue(data);
                    adapter.notifyDataSetChanged();
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });

            Toast.makeText(this, "Member Added", Toast.LENGTH_SHORT).show();

            dialog.dismiss();

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}