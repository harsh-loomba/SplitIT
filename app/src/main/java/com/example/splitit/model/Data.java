package com.example.splitit.model;

import java.util.List;

public class Data {
    private String groupName;
    private List<String> memberNames;
    private List<Float> amountPaid;

    public Data(String groupName,List<String> memberNames, List<Float> amountPaid
    ) {
        this.groupName = groupName;
        this.memberNames = memberNames;
        this.amountPaid = amountPaid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames;
    }

    public List<Float> getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(List<Float> amountPaid) {
        this.amountPaid = amountPaid;
    }

}
