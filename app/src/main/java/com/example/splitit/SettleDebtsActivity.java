package com.example.splitit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SettleDebtsActivity extends AppCompatActivity {

    private DatabaseReference groupsDatabase;

    private List<String> member_names = new ArrayList<>();
    private final List<Float> amount_paid = new ArrayList<>();
    private final List<Pair<String,Float>> Settle_Debts = new ArrayList<>();
    String group_name;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_debts);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        group_name = getIncomingIntent();
        getSupportActionBar().setTitle("Settle Debts - " + group_name);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        groupsDatabase = FirebaseDatabase.getInstance().getReference().child(firebaseUser.getUid());

        member_names.add("Harsh");
        member_names.add("Vansh");
        member_names.add("Sam");
        member_names.add("Tom");
        member_names.add("Gary");
        amount_paid.add((float)1200);
        amount_paid.add((float)500);
        amount_paid.add((float)680);
        amount_paid.add((float)2000);
        amount_paid.add((float)260);

        LoadData();
        GenerateSettleDebtList();

        for (Pair<String, Float> p : Settle_Debts) {
            System.out.println(p.first + " = " + p.second);
        }

        System.out.println(member_names);
        System.out.println(amount_paid);

        RecyclerView recyclerView = this.findViewById(R.id.settle_debts_recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        SettleDebtsAdapter adapter = new SettleDebtsAdapter(this, Settle_Debts);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    private void LoadData() {

        groupsDatabase.child("memberNames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    member_names.clear();
                    for(DataSnapshot dss : snapshot.child(group_name).getChildren()){
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
                    for(DataSnapshot dss : snapshot.child(group_name).getChildren()){
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

    public static class SettleDebtsAdapter extends RecyclerView.Adapter<SettleDebtsViewHolder>{

        private final Context context;
        private final List<Pair<String,Float>> settle_debts;

        public SettleDebtsAdapter(Context context, List<Pair<String, Float>> Settle_debts) {
            this.context = context;
            this.settle_debts = Settle_debts;
        }


        @NonNull
        @Override
        public SettleDebtsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SettleDebtsViewHolder(LayoutInflater.from(context).inflate(R.layout.settle_debts_recycler_data, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SettleDebtsViewHolder holder, int position) {
            holder.setSettleData(settle_debts.get(position).first , settle_debts.get(position).second);
        }

        @Override
        public int getItemCount() {
            return settle_debts.size();
        }
    }

    public static class SettleDebtsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView mMemberPay;
        TextView mAmountPay;

        public SettleDebtsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mMemberPay = itemView.findViewById(R.id.member_pay_recycler);
            mAmountPay = itemView.findViewById(R.id.amount_pay_recycler);
        }

        private void setSettleData(String member_Pay, Float amount_Pay){
            TextView myMemberPay = mView.findViewById(R.id.member_pay_recycler);
            TextView myAmountPay = mView.findViewById(R.id.amount_pay_recycler);
            myMemberPay.setText(member_Pay);
            myAmountPay.setText(String.valueOf(amount_Pay));
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

    public static class mPair{
        private Float first;
        private String second;

        public mPair(Float first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    public static class sortPair implements Comparator<mPair>
    {
        @Override
        public int compare(mPair o1, mPair o2) {
            return Float.compare(o1.first, o2.first);
        }
    }

    private void GenerateSettleDebtList(){
        List<mPair> meanAmt = new ArrayList<>();
        Float average = (float)0;
        int n = member_names.size();

        for(int i = 0; i < n; i++){
            average += amount_paid.get(i);
        }

        average /= n;

        for(int i = 0; i < n; i++){
            meanAmt.add(new mPair((amount_paid.get(i) - average), member_names.get(i)));
        }

        Collections.sort(meanAmt, new sortPair());

        int i = 0;
        int j = n - 1;

        while(i < j){

            if(meanAmt.get(i).first > 0){
                break;
            }

            if(meanAmt.get(j).first < 0){
                break;
            }

            if(meanAmt.get(i).first == 0){
                i++;
                continue;
            }

            if(meanAmt.get(j).first == 0){
                j--;
                continue;
            }

            if((meanAmt.get(j).first + meanAmt.get(i).first) >= 0){
                Settle_Debts.add(Pair.create((meanAmt.get(i).second + " -> " + meanAmt.get(j).second), ((float)0 - meanAmt.get(i).first)));
                meanAmt.get(j).first += meanAmt.get(i).first;
                meanAmt.get(i).first = (float)0;
            }
            else{
                Settle_Debts.add(Pair.create((meanAmt.get(i).second + " -> " + meanAmt.get(j).second), meanAmt.get(j).first));
                meanAmt.get(i).first += meanAmt.get(j).first;
                meanAmt.get(j).first = (float)0;
            }
        }

    }
}