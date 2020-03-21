package thkContract;

import org.web3j.abi.datatypes.Type;

/**
 * @author hcy
 * @version 1.0
 * @date 2020/3/20 下午7:45
 */

public class BusinessObj implements Type {

    private  String dataNo;
    private  String data;

    public String getDataNo() {
        return dataNo;
    }

    public void setDataNo(String dataNo) {
        this.dataNo = dataNo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String getTypeAsString() {
        return "(string,string)";
    }
}
