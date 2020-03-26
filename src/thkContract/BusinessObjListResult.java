package thkContract;

import org.web3j.abi.datatypes.Type;

import java.util.List;

/**
 * @author hcy
 * @version 1.0
 * @date 2020/3/26 上午10:21
 */
public class BusinessObjListResult  implements Type {
    public List<BusinessObj> getBusinessObjList() {
        return businessObjList;
    }

    public void setBusinessObjList(List<BusinessObj> businessObjList) {
        this.businessObjList = businessObjList;
    }

    private List<BusinessObj> businessObjList;

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getTypeAsString() {
        return null;
    }
}
