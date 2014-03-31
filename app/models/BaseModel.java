package models;

import javax.persistence.*;
import play.db.ebean.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
public abstract class BaseModel extends Model {

    @Id
    public UUID id = UUID.randomUUID();
    
    @Column(columnDefinition="text")
    public String remarks;
    @Column(columnDefinition="text")
    public String ext;
    public UUID createdBy;
    public Timestamp createdAt;
    public UUID updatedBy;
    public Timestamp updatedAt;
    public boolean deleted = false;

    @Version
    public long version;

    @Column(length=40)
    public String schemaCode;

}

