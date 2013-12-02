package controllers;

import play.*;
import play.mvc.*;

import models.*;

import play.data.validation.Required;
import javax.persistence.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Application extends Controller {
    
    //パスワード暗号化
    public static String bytesToHexString(byte[] bytes) {
        final StringBuffer sb = new StringBuffer();
        for (final byte b: bytes) {
            final String s = Integer.toHexString(0xff & b);
            sb.append(s.length() == 1 ? "0" + s : s);
        }
        return sb.toString();
    }

    public static String getSHA256(String s) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = s.getBytes();
            digest.update(bytes, 0, bytes.length);
            
            return bytesToHexString(digest.digest());
        }
        catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    //トップページ
    public static void index() {
        render();
    }
    
    //ログイン画面
    public static void loginform(){
        if(!session.contains("signinerror") && !session.contains("signup") && !session.contains("signuperror")){
            render();
        }else if(session.contains("signinerror")){
            session.clear();
            int stt = 1;
            render(stt);
        }else if(session.contains("signup")){
            session.clear();
            int stt = 2;
            render(stt);
        }else if(session.contains("signuperror")){
            if(session.get("signuperror").equals("1")){
                session.clear();
                int stt = 3;
                render(stt);
            }else if(session.get("signuperror").equals("2")){
                session.clear();
                int stt = 4;
                render(stt);
            }
        }else{
            render();
        }
    }
    //ログイン処理
    public static void signin(){
        //アカウント情報取得
        List<UserData> acc = UserData.find("userid = ? and password = ?", params.get("userID"), getSHA256(params.get("password") + params.get("userID"))).fetch();
        //取得したアカウントの件数で場合分け
        if(acc.size() == 1){
            //管理人だった場合
            if(acc.get(0).admin == 1){
                session.put("status","admin");
                statuscontroller();
            //一般ユーザーの場合
            }else{
                String user = acc.get(0).userid;
                session.put("status","todo");
                session.put("user",user);
                statuscontroller();
            }
        //取得しなかった場合
        }else{
            session.put("signinerror","1");
            loginform();
        }
    }
    
    public static void createaccount() {
        if(!params.get("userID").equals("") && !params.get("password").equals("")){
            if(params.get("password").equals(params.get("cppass"))){
                if(UserData.find("userid = ?", params.get("userID")).fetch().size() == 0){
                    String userid = params.get("userID");
                    String password = getSHA256(params.get("password") + userid);
                    UserData usr = new UserData(userid,password,0);
                    usr.save();
                    session.put("signup","1");
                    loginform();
                }else{
                    session.put("signuperror","1");
                    loginform();
                }
            }else{
                session.put("signuperror","2");
                loginform();
            }
        }else{
            session.put("signuperror","2");
            loginform();
        }
    }
    
    //ログイン後管理人と一般ユーザーの遷移先管理
    public static void statuscontroller(){
        if(!session.contains("status")){
            error();
        }else if(session.get("status") == null || session.get("status") == ""){
            error();
        }else if(session.get("status").equals("todo")){
            todolist(0,0,0);
        }else if(session.get("status").equals("admin")){
            accountlist();
        }
    }
    //todoリストの表示
    public static void todolist(int q, int s,int page){
        //直接urlでアクセスしてきた場合の判定
        if(!session.contains("status")){
            error();
        }else if(session.get("status") == null || session.get("status") == ""){
            error();
        }else if(session.get("status").equals("todo")){
            //取得するタスク10件のid管理
            String user = session.get("user");
            int taskid = 0;
            int pid = 0;
            int pbid = 0;
            if(page >= 0){
                pid = page + 1;
                taskid = page * 10;
            }
            if(pid < 2){
                pbid = 0;
            }else{
                pbid = pid - 2;
            }
            System.out.println("taskid = "+ taskid);
            System.out.println("pid = "+pid);
            System.out.println("pbid = "+pbid);
            System.out.println("page = "+page);
            System.out.println("s = "+s);
            System.out.println("q = "+ q);
            //取得する期限で分岐
            String timelimit = "今日";
            if(q == 1){
                timelimit = "今日";
            }else if(q == 2){
                timelimit = "明日";
            }else if(q == 3){
                timelimit = "明後日";
            }else if(q == 4){
                timelimit = "近日中";
            }else if(q == 5){
                timelimit = "将来";
            }else{
                //未完のみで全部表示する場合
                if(s == 1){
                    List<TodoList> tsks = TodoList.find("userid = ? and status = ? ", user, 0).fetch();
                    System.out.println("task.size = "+ tsks.size());
                    System.out.println("task.size - taskid = "+ (tsks.size() - taskid));
                    if(tsks.size() - taskid ==  0){
                        pid = page;
                        Collections.sort(tsks, new timelimitsort());
                        List<TodoList> tsk;
                        //そもそもタスクが0
                        if(taskid == 0){
                            tsk = tsks.subList(taskid, taskid);
                        //取得したタスクのリスト数が丁度0
                        }else{
                            tsk = tsks.subList(taskid - 10, taskid);
                        }
                        tsks = tsk;
                    //取得したタスクが10件未満だった場合
                    }else if(tsks.size() - taskid < 10){
                        pid = page;
                        Collections.sort(tsks, new timelimitsort());
                        List<TodoList> tsk = tsks.subList(taskid,tsks.size());
                        tsks = tsk;
                    }else{
                        Collections.sort(tsks, new timelimitsort());
                        List<TodoList> tsk = tsks.subList(taskid, taskid + 10);
                        tsks = tsk;
                    }
                    render(user,tsks,s,pid,pbid);
                //全部表示する場合
                }else{
                    List<TodoList> tsks = TodoList.find("userid = ?", user).fetch();
                    if(tsks.size() - taskid == 0){
                        pid = page;
                        Collections.sort(tsks, new timelimitsort());
                        List<TodoList> tsk;
                        if(taskid == 0){
                            tsk = tsks.subList(taskid, taskid);
                        }else{
                            tsk = tsks.subList(taskid - 10, taskid);
                        }
                        tsks = tsk;
                    }else if(tsks.size() - taskid < 10){
                        pid = page;
                        Collections.sort(tsks, new timelimitsort());
                        List<TodoList> tsk = tsks.subList(taskid, tsks.size());
                        tsks = tsk;
                    }else{
                        Collections.sort(tsks, new timelimitsort());
                        List<TodoList> tsk = tsks.subList(taskid, taskid + 10);
                        tsks = tsk;
                    }
                    render(user,tsks,pid,pbid);
                }
            }
            //未完のみで期間ごとに表示する場合
            if(s == 1){
                List<TodoList> tsks = TodoList.find("userid = ? and timelimit = ? and status = ?", user, timelimit, 0).from(taskid).fetch(10);
                if(tsks.size() < 10){
                    pid = page;
                }
                if(tsks.size() == 0 && taskid >= 10){
                    tsks = TodoList.find("userid = ? and timelimit = ? and status = ?", user, timelimit, 0).from(taskid - 10).fetch(10);
                    pid = page;
                }
                Collections.sort(tsks, new timelimitsort());
                render(user,tsks,q,s,pid,pbid);
            //期間ごとに表示する場合
            }else{
                List<TodoList> tsks = TodoList.find("userid = ? and timelimit = ?", user, timelimit).from(taskid).fetch(10);
                if(tsks.size() < 10){
                    pid = page;
                }
                if(tsks.size() == 0 && taskid >= 10){
                    tsks = TodoList.find("userid = ? and timelimit = ?", user, timelimit).from(taskid - 10).fetch(10);
                    pid = page;
                }
                Collections.sort(tsks, new timelimitsort());
                render(user,tsks,q,pid,pbid);
            }
        }else if(session.get("status").equals("admin")){
            accountlist();
        }
    }
    
    /*public static void todolist(){
        if(!session.contains("status")){
            error();
        }else if(session.get("status") == null || session.get("status") == ""){
            error();
        }else if(session.get("status").equals("todo")){
            String user = session.get("user");
            List<TodoList> tsks = TodoList.find("userid = ?", user).from(0).fetch(10);
            Collections.sort(tsks, new timelimitsort());
            int pid = 1;
            int q = 0;
            int pbid = 1;
            render(user,tsks,q,pid,pbid);
        }else if(session.get("status").equals("admin")){
            accountlist();
        }
    }*/
    //管理人画面用
    public static void accountlist(){
        List<UserData> users = UserData.all().fetch();
        if(!session.contains("status")){
            error();
        }else if(session.get("status") == null || session.get("status") == ""){
            error();
        }else if(session.get("status").equals("todo")){
            todolist(0,0,0);
        }else if(session.get("status").equals("admin")){
            render(users);
        }
    }
    //エラーページ
    public static void error(){
        render();
    }
    //ログアウト
    public static void logout(){
        session.clear();
        render();
    }
    //管理者がアカウントを削除する際使用
    public static void deleteaccount(){
        UserData d = UserData.find("userid = ?", params.get("userID")).first();
        d.delete();
        accountlist();
    }
    
    //タスクを追加する
    public static void addtask(){
        System.out.println(params.get("task"));
        if(params.get("task") == null || params.get("task").equals("")){
            todolist(0,0,0);
        }else{
            String userid = session.get("user");
            //現在の時間の取得
            Calendar cal1 = Calendar.getInstance();
            int year = cal1.get(Calendar.YEAR);
            int month = cal1.get(Calendar.MONTH) + 1;
            int day = cal1.get(Calendar.DATE);
            int hour = cal1.get(Calendar.HOUR_OF_DAY);
            int minute = cal1.get(Calendar.MINUTE);
            String date = year + "-" + month + "-" + day + " " + hour + ":" + minute;
            String task = params.get("task");
            String timelimit = params.get("timelimit");
            TodoList tsk = new TodoList(userid,date,task,timelimit,0);
            tsk.save();
            todolist(0,0,0);
        }
        todolist(0,0,0);
    }
    //タスクを編集・削除する
    public static void edittask(){
        String delete_id = params.get("delete");
        String finish_id = params.get("finish");
        String edit_id = params.get("edit");
        //タスクの編集ページに移動
        if(edit_id != null){
            TodoList t = TodoList.find("id = ?", Long.valueOf(edit_id)).first();
            if(t == null){
                todolist(0,0,0);
            }else{
                int tl = t.getTL();
                render(t, tl);
            }
        }else if(delete_id == null){
            //urlでのアクセス時
            if(finish_id == null){
                todolist(0,0,0);
            //タスクが完了した場合
            }else if(!finish_id.equals("")){
                TodoList d = TodoList.find("id = ?", Long.valueOf(finish_id)).first();
                if(d == null){
                    todolist(0,0,0);
                }else{
                    int status = d.changestatus();
                    d.save();
                    todolist(0,0,0);
                }
            }else{
                todolist(0,0,0);
            }
        //タスクを削除する場合
        }else if(!delete_id.equals("")){
            TodoList d = TodoList.find("id = ?", Long.valueOf(delete_id)).first();
            if(d == null){
                todolist(0,0,0);
            }else{
                d.delete();
                todolist(0,0,0);
            }
        }else{
            todolist(0,0,0);
        }
    }
    //タスクを編集する場合
    public static void edit(){
        String noedit = params.get("noedit");
        String edit = params.get("edit");
        if(noedit != null){
            todolist(0,0,0);
        }else if(edit != null){
            TodoList t = TodoList.find("id = ?", Long.valueOf(params.get("id"))).first();
            if(t == null){
                todolist(0,0,0);
            }else{
                String ch_timelimit = params.get("timelimit");
                String ch_task = params.get("task");
                String ch_status = params.get("stt");
                t.changetask(ch_task);
                t.changedate(ch_timelimit);
                t.changestt(ch_status);
                t.save();
            }
        }
        todolist(0,0,0);
    }
    
    public static void statuschange(@Required String p){
        TodoList d = TodoList.find("id = ?", Long.valueOf(p)).first();
        int status = d.changestatus();
        d.save();
        // Map に結果を蓄え，JSON として出力
        Map<String, Object> map = new HashMap<String, Object>();
        if(status == 0){
            String result = "未";
            map.put("result", "未");
        }else{
            map.put("result", "完");
        }
        renderJSON(map);
    }
}
