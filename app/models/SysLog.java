//SysLog.java
package models;

import javax.persistence.*;
import play.db.ebean.Model;

import java.util.UUID;
import java.sql.Timestamp;

@Entity
@Table(name="t_sys_log")
public class SysLog extends BaseModel {

    public UUID businessId;
    public UUID companyId;
    public UUID warehouseId;
    public UUID employeeId;
    public UUID userId;
    @Column(length=40)
    public String logLevel;
    public Timestamp logTimestamp;
    @Column(columnDefinition="text")
    public String logContent;

    public static Finder<String, SysLog> find() {
        return new Finder<String, SysLog>(String.class, SysLog.class);
    }
}

