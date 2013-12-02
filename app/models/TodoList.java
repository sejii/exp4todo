package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class TodoList extends Model {
    /**
     * userid
     */
    public String userid;
    /**
     * date
     */
    public String date;
    /**
     * title
     */
    public String task;
    /**
     * time
     */
    public String timelimit;
    /**
     * status
     */
    public int status;

    /**
     * コンストラクタ．
     * @param userid ユーザーID
     * @param date 日時
     * @param task タスクタイトル
     * @param timelimit 期日
     * @param status 状態(0 = 未完了、1 = 完了)
     */
    public TodoList(String userid, String date, String task, String timelimit, int status) {
        this.userid = userid;
        this.date = date;
        this.task = task;
        this.timelimit = timelimit;
        this.status = status;
    }
    
    public int getTL(){
        if(timelimit.equals("今日")){
            return 0;
        }else if(timelimit.equals("明日")){
            return 1;
        }else if(timelimit.equals("明後日")){
            return 2;
        }else if(timelimit.equals("近日中")){
            return 3;
        }else if(timelimit.equals("将来")){
            return 4;
        }else{
            return -1;
        }
    }
    public int changestatus(){
        this.status = 1-this.status;
    	return this.status;
    }
    public void changetask(String task){
        if(!this.task.equals(task)){
            this.task = task;
        }
    }
    public void changedate(String timelimit){
        if(!this.timelimit.equals(timelimit)){
            this.timelimit = timelimit;
        }
    }
    public void changestt(String status){
        int stt = 0;
        if(status.equals("完")){
            stt = 1;
        }else{
            stt = 0;
        }
        if(this.status != stt){
            this.status = stt;
        }
    }
}
