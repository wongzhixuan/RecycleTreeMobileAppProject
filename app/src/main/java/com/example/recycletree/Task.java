package com.example.recycletree;

public class Task {
    private String task_name;
    private int reward_pts;
    public Task(String task_name, int reward_pts){
        this.task_name = task_name;
        this.reward_pts = reward_pts;
    }

    public String getTask_name(){
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public int getReward_pts() {
        return reward_pts;
    }

    public void setReward_pts(int reward_pts) {
        this.reward_pts = reward_pts;
    }
}
