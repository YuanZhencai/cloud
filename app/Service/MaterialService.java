package Service;

import java.util.ArrayList;
import java.util.List;

import models.vo.outbound.cargoPlanSplitVo;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import Service.Vo.SMaterial;


public class MaterialService {
    public static String NEW="S000";
 
    
    public static SMaterial getMaterialbyCode(String material,String Owner){
        SMaterial result = (SMaterial) DateService.sendHttp("/material/bycode/"+Owner+"/"+material,"GET",null,new SMaterial());
        return result;
    }
    
    public static List<SMaterial> getMaterialbyowner(String Owner){
        List<SMaterial> result =  (List<SMaterial>) DateService.sendHttp("/material/byowner/"+Owner,"GET",null,new ArrayList<SMaterial>(),new TypeReference<List<SMaterial>>() {
        });
        return result;
    }
    
    public static SMaterial putMaterialbyCode(SMaterial SMaterial,String Owner){
        SMaterial result = (SMaterial) DateService.sendHttp("/material/bycode/"+Owner+"/"+SMaterial.name,"PUT",SMaterial,new SMaterial());
        return result;
    }
    
    public static SMaterial deleteMaterial(SMaterial smaterial,String Owner){
        SMaterial result = (SMaterial) DateService.sendHttp("/material/bycode/"+Owner+"/"+smaterial.name,"DELETED",null,new SMaterial());
        return result;
    }
    
}   
