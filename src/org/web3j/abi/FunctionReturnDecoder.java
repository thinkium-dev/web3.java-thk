package org.web3j.abi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.BytesType;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;
import thkContract.BusinessObjListResult;

import static org.web3j.abi.TypeDecoder.MAX_BYTE_LENGTH_FOR_HEX_STRING;

/**
 * Decodes values returned by function or event calls.
 */
public class FunctionReturnDecoder {

    private FunctionReturnDecoder() { }

    /**
     * Decode ABI encoded return values from smart contract function call.
     *
     * @param rawInput ABI encoded input
     * @param outputParameters list of return types as {@link TypeReference}
     * @return {@link List} of values returned by function, {@link Collections#emptyList()} if
     *         invalid response
     */
    public static List<Type> decode(
            String rawInput, List<TypeReference<Type>> outputParameters) {
        String input = Numeric.cleanHexPrefix(rawInput);

        if (Strings.isEmpty(input)) {
            return Collections.emptyList();
        } else {
            return build(input, outputParameters);
        }
    }

    /**
     * <p>Decodes an indexed parameter associated with an event. Indexed parameters are individually
     * encoded, unlike non-indexed parameters which are encoded as per ABI-encoded function
     * parameters and return values.</p>
     *
     * <p>If any of the following types are indexed, the Keccak-256 hashes of the values are
     * returned instead. These are returned as a bytes32 value.</p>
     *
     * <ul>
     *     <li>Arrays</li>
     *     <li>Strings</li>
     *     <li>Bytes</li>
     * </ul>
     *
     * <p>See the
     * <a href="http://solidity.readthedocs.io/en/latest/contracts.html#events">
     * Solidity documentation</a> for further information.</p>
     *
     * @param rawInput ABI encoded input
     * @param typeReference of expected result type
     * @param <T> type of TypeReference
     * @return the decode value
     */
    @SuppressWarnings("unchecked")
    public static <T extends Type> Type decodeIndexedValue(
            String rawInput, TypeReference<T> typeReference) {
        String input = Numeric.cleanHexPrefix(rawInput);

        try {
            Class<T> type = typeReference.getClassType();

            if (Bytes.class.isAssignableFrom(type)) {
                return TypeDecoder.decodeBytes(input, (Class<Bytes>) Class.forName(type.getName()));
            } else if (Array.class.isAssignableFrom(type)
                    || BytesType.class.isAssignableFrom(type)
                    || Utf8String.class.isAssignableFrom(type)) {
                return TypeDecoder.decodeBytes(input, Bytes32.class);
            } else {
                return TypeDecoder.decode(input, type);
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    private static List<Type> build(
            String input, List<TypeReference<Type>> outputParameters) {
        List<Type> results = new ArrayList<>(outputParameters.size());

        int offset = 0;
        for (TypeReference<?> typeReference:outputParameters) {
            try {
                @SuppressWarnings("unchecked")
                Class<Type> type = (Class<Type>) typeReference.getClassType();

                int hexStringDataOffset = getDataOffset(input, offset, type);

                Type result;
                if (DynamicArray.class.isAssignableFrom(type)) {
                    result = TypeDecoder.decodeDynamicArray(
                            input, hexStringDataOffset, typeReference);
                    offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;

                } else if (typeReference instanceof TypeReference.StaticArrayTypeReference) {
                    int length = ((TypeReference.StaticArrayTypeReference) typeReference).getSize();
                    result = TypeDecoder.decodeStaticArray(
                            input, hexStringDataOffset, typeReference, length);
                    offset += length * MAX_BYTE_LENGTH_FOR_HEX_STRING;

                } else if (StaticArray.class.isAssignableFrom(type)) {
                    int length = Integer.parseInt(type.getSimpleName()
                            .substring(StaticArray.class.getSimpleName().length()));
                    result = TypeDecoder.decodeStaticArray(
                            input, hexStringDataOffset, typeReference, length);
                    offset += length * MAX_BYTE_LENGTH_FOR_HEX_STRING;

                } else if (BusinessObj.class.isAssignableFrom(type)) {
//                    int length = Integer.parseInt(type.getSimpleName()
//                            .substring(StaticArray.class.getSimpleName().length()));
//                    result = TypeDecoder.decodeStaticArray(
//                            input, hexStringDataOffset, typeReference, length);
//                    offset += length * MAX_BYTE_LENGTH_FOR_HEX_STRING;

                    BusinessObj.BusinessObjResult decodeBusinessObj= decodeBusinessObj(input, offset);
                    result=(BusinessObj)decodeBusinessObj;
                    offset=decodeBusinessObj.getOffset();

                } else if (BusinessObjListResult.class.isAssignableFrom(type)) {
                    offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
                    int resultCode= org.web3j.abi.TypeDecoder.decodeUintAsInt(input,offset);
                    System.out.println(resultCode);
                    offset += resultCode*MAX_BYTE_LENGTH_FOR_HEX_STRING;

                    BusinessObjListResult objListResult=new BusinessObjListResult();

                    List<BusinessObj> businessObjList=new ArrayList<>();
                    for (Integer i=0;i<resultCode;i++){
                        System.out.println(i.toString()+":==> "+offset);
                        BusinessObj.BusinessObjResult decodeBusinessObj= decodeBusinessObj(input, offset);
                        BusinessObj resultInfo=(BusinessObj)decodeBusinessObj;
                        offset=decodeBusinessObj.getOffset();
                        businessObjList.add(resultInfo);
                    }
                    objListResult.setBusinessObjList(businessObjList);
                    result=objListResult;
                }else {
                    result = TypeDecoder.decode(input, hexStringDataOffset, type);
                    offset += MAX_BYTE_LENGTH_FOR_HEX_STRING;
                }
                results.add(result);

            } catch (ClassNotFoundException e) {
                throw new UnsupportedOperationException("Invalid class reference provided", e);
            }
        }
        return results;
    }
    public   static BusinessObj.BusinessObjResult decodeBusinessObj(String inputCode, int offset) {
        BusinessObj.BusinessObjResult result=new BusinessObj.BusinessObjResult();

        String input=inputCode;
        String destr="0000000000000000000000000000000000000000000000000000000000000020";
        input=input.substring(destr.length());

        //datano
        int valueOffset = offset+MAX_BYTE_LENGTH_FOR_HEX_STRING+MAX_BYTE_LENGTH_FOR_HEX_STRING;
        Utf8String d= TypeDecoder.decodeUtf8String(input, valueOffset);
        System.out.println(d);
        result.setDataNo(d.toString());

        //data
        int encodedLength = TypeDecoder.decodeUintAsInt(input, valueOffset);
        valueOffset = valueOffset + ((encodedLength / Type.MAX_BYTE_LENGTH) + 2) * MAX_BYTE_LENGTH_FOR_HEX_STRING;
        Utf8String  d1= TypeDecoder.decodeUtf8String(input, valueOffset);
        result.setData(d1.toString());

        encodedLength = TypeDecoder.decodeUintAsInt(input, valueOffset);
        valueOffset = valueOffset + ((encodedLength / Type.MAX_BYTE_LENGTH) + 2) * MAX_BYTE_LENGTH_FOR_HEX_STRING;

        result.setOffset(valueOffset);
        return result;
    }

    private static <T extends Type> int getDataOffset(String input, int offset, Class<T> type) {
        if (DynamicBytes.class.isAssignableFrom(type)
                || Utf8String.class.isAssignableFrom(type)
                || DynamicArray.class.isAssignableFrom(type)) {
            return TypeDecoder.decodeUintAsInt(input, offset) << 1;
        } else {
            return offset;
        }
    }
}
