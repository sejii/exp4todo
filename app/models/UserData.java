package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class UserData extends Model {
    /**
     * UserId
     */
    public String userid;
    /**
     * password
     */
    public String password;
    /**
     * admin
     */
    public int admin;

    /**
     * コンストラクタ．
     * @param userid ユーザーID
     * @param password パスワード
     * @param admin 管理者権限
     */
    public UserData(String userid, String password, int admin) {
        this.userid = userid;
        this.password = password;
        this.admin = admin;
    }
}
