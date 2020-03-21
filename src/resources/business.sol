pragma experimental ABIEncoderV2;
pragma solidity ^0.5.0;

contract Business {

    struct BusinessObj {
        string dataNo; //上传的数据标识（唯一id）
        string data; //上传的数据内容（多个字段json序列化）
        //string status;//上传的数据状态
    }

    uint64 private dataNum = 0;//上传的数据数量
    uint64 private historyNum = 0;
    address public owner;
    mapping(uint64 => bool) private existDatas; //判断数据是否存在
    mapping(uint64 => BusinessObj) private objects;
    mapping(string => uint64) private id2record;

    event NewObject(string dataNo);
    event ModifyObject(string dataNo);


    constructor() public{
        owner = msg.sender;
    }

    //判断记录是否存在
    function isRecordExist(uint64 recordId) public view returns (bool isIndeed) {
        return (existDatas[recordId] == true);

    }

    //判断数据是否存在
    function isDataExist(string memory dataNo) public view returns (bool isIndeed) {
        uint64 recordId;
        recordId = id2record[dataNo];
        return (existDatas[recordId] == true);

    }

    //添加数据
    function createObj(BusinessObj memory bObj) public returns (uint64 totalNum){
        require(msg.sender == owner);
        bool dataExist = isDataExist(bObj.dataNo);
        require(dataExist == false);

        //数据不存在

        id2record[bObj.dataNo] = historyNum + 1;
        objects[historyNum + 1] = bObj;

        uint64 recordId;
        recordId = id2record[bObj.dataNo];
        existDatas[recordId] = true;
        emit NewObject(bObj.dataNo);
        dataNum++;
        historyNum++;
        return historyNum;
    }

    //分页数据 _offset从1开始  _limit取值 1到100
    function getAllObjs(uint64 _offset, uint64 _limit) public view returns (uint64 totalNum, uint64 offset, uint64 limit, BusinessObj[] memory objs){
        require(msg.sender == owner);
        require(_limit >= 1 && _limit <= 100);
        BusinessObj[] memory _objs;
        if (dataNum < _offset + _limit - 1) {
            _objs = new BusinessObj[](dataNum - _offset + 1);
        }

        else {
            _objs = new BusinessObj[](_limit);
        }

        uint64 curNum = 0;
        uint64 curLimit = 0;
        for (uint64 num = 1; num <= historyNum; num++) {
            if (existDatas[num]) {
                curNum++;
                if (curNum >= _offset) {

                    _objs[curLimit] = objects[num];
                    curLimit++;
                    if (curLimit >= _limit) {
                        break;
                    }
                }
            }
        }
        return (dataNum, _offset, _limit, _objs);
    }

    //根据标识获取数据
    function getObjById(string memory dataNo) public view returns (BusinessObj memory obj){
        require(msg.sender == owner || isDataExist(dataNo));
        uint64 recordId = id2record[dataNo];
        BusinessObj storage newObject = objects[recordId];
        return newObject;
    }

    //根据标识更新数据
    function updateObj(BusinessObj memory bObj, string memory dataNo) public returns (bool result){
        require(msg.sender == owner || isDataExist(dataNo));
        uint64 recordId = id2record[dataNo];
        objects[recordId] = bObj;
        existDatas[recordId] = true;
        delete id2record[dataNo];
        id2record[bObj.dataNo] = recordId;
        emit ModifyObject(bObj.dataNo);

        return true;
    }

    //根据标识删除数据
    function deleteObj(string memory dataNo) public returns (bool result){

        require(msg.sender == owner || isDataExist(dataNo));
        uint64 recordId = id2record[dataNo];
        delete objects[recordId];
        delete existDatas[recordId];
        delete id2record[dataNo];

        if (dataNum >= 1)
            dataNum = dataNum - 1;
        return true;
    }

    //获取数据数量
    function getObjsNum() public view returns (uint64 totalNum){
        return (dataNum);
    }
}