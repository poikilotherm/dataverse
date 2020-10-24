
package edu.harvard.iq.dataverse.api.dto;

import edu.harvard.iq.dataverse.util.json.JsonUtil;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author ellenk
 */
public class FieldDTO {

    public FieldDTO() {
    }
    
    public static FieldDTO createPrimitiveFieldDTO(String typeName, String value) {
        FieldDTO primitive = new FieldDTO();
        primitive.typeName=typeName;
        primitive.setSinglePrimitive(value);
        return primitive;
    }
    public static FieldDTO createMultiplePrimitiveFieldDTO(String typeName, List<String> values) {
        FieldDTO primitive = new FieldDTO();
        primitive.typeName=typeName;
        primitive.setMultiplePrimitive(values);
        return primitive;
    }
    public static FieldDTO createMultipleVocabFieldDTO(String typeName, List<String> values) {
        FieldDTO primitive = new FieldDTO();
        primitive.typeName=typeName;
        primitive.setMultipleVocab(values);
        return primitive;
    }   
    public static FieldDTO createVocabFieldDTO(String typeName, String value) {
        FieldDTO field = new FieldDTO();
        field.typeName=typeName;
        field.setSingleVocab(value);
        return field;
        
    }
    public static FieldDTO createCompoundFieldDTO(String typeName, FieldDTO... value) {
        FieldDTO field = new FieldDTO();
        field.typeName=typeName;
        field.setSingleCompound(value);
        return field;
        
    }
     
    /**
     * Creates a FieldDTO that contains a size=1 list of compound values 
     * @param typeName
     * @param value
     * @return 
     */  
    public static FieldDTO createMultipleCompoundFieldDTO(String typeName, FieldDTO... value) {
        FieldDTO field = new FieldDTO();
        field.typeName = typeName;
        field.setMultipleCompound(value);
        return field;
    } 
    public static FieldDTO createMultipleCompoundFieldDTO(String typeName,List<HashSet<FieldDTO>> compoundList) {
        FieldDTO field = new FieldDTO();
        field.typeName=typeName;
        field.setMultipleCompound(compoundList);
        return field;
    }
    
    String typeName;
    Boolean multiple;
    String typeClass;
    
    /**
     * The contents of {@link #value} depend on the field attributes:
     * - default init with JSON null
     * - if single/primitive, value is a String
     * - if multiple, value is a JsonArray
     *     - multiple/primitive: each JsonArray element will contain String
     *     - multiple/compound: each JsonArray element will contain Set of FieldDTOs
    */
    JsonValue value = JsonValue.NULL;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public String getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(String typeClass) {
        this.typeClass = typeClass;
    }

    public String getSinglePrimitive() {
        return JsonUtil.getPrimitiveAsString(value);
    }
    
    String getSingleVocab() {
        return getSinglePrimitive();
    }
    
    Set<FieldDTO> getSingleCompound(JsonValue value) {
        return value.asJsonObject()
               .entrySet()
               .stream()
               .map(entry -> JsonUtil.fromJson(entry.getValue(), FieldDTO.class))
               .collect(Collectors.toSet());
    }
    
    public Set<FieldDTO> getSingleCompound() {
        return getSingleCompound(this.value);
    }

    public List<String> getMultiplePrimitive() {
        return this.value
                   .asJsonArray()
                   .getValuesAs(JsonString.class)
                   .stream()
                   .map(JsonString::getString)
                   .collect(Collectors.toList());
    }

    public List<String> getMultipleVocab() {
        return this.getMultiplePrimitive();
    }

    public ArrayList<HashSet<FieldDTO>> getMultipleCompound() {
        return new ArrayList<>(
            this.value
                .asJsonArray()
                .stream()
                .map(element -> new HashSet<>(this.getSingleCompound(element)))
                .collect(Collectors.toList())
        );
    }

    public void setSinglePrimitive(String value) {
        Gson gson = new Gson();
        typeClass = "primitive";
        multiple = false;
        this.value = gson.toJsonTree(value, String.class);
    }

    public void setSingleVocab(String value) {
         Gson gson = new Gson();
         typeClass = "controlledVocabulary";
        multiple = false;
        this.value = gson.toJsonTree(value);
    }
    
    public void setSingleCompound(FieldDTO... compoundField) {
        Gson gson = new Gson();
        this.typeClass = "compound";
        this.multiple = false;
       
        JsonObject obj = new JsonObject();
        for (FieldDTO fieldDTO : compoundField) {
            if (fieldDTO!=null) {
                obj.add(fieldDTO.typeName,gson.toJsonTree(fieldDTO, FieldDTO.class));
            }
        }
  
        this.value = obj;
    }
    public void setMultiplePrimitive(String[] value) {
        Gson gson = new Gson();
        typeClass = "primitive";
        multiple = true;
        this.value = gson.toJsonTree(Arrays.asList(value));
    }
    public void setMultiplePrimitive(List<String> value) {
        Gson gson = new Gson();
        typeClass = "primitive";
        multiple = true;
        this.value = gson.toJsonTree(value);
    }

    public void setMultipleVocab(List<String> value) {
       Gson gson = new Gson();
       typeClass = "controlledVocabulary";
        multiple = true;
        this.value = gson.toJsonTree(value);
    }

    /**
     * Set value to a size 1 list of compound fields, that is a single set of primitives
     */
    public void setMultipleCompound(FieldDTO... fieldList) {
         Gson gson = new Gson();
        this.typeClass = "compound";
        this.multiple = true;
        List<Map<String, FieldDTO>> mapList = new ArrayList<Map<String, FieldDTO>>();
            Map<String, FieldDTO> fieldMap = new HashMap<>();
            for (FieldDTO fieldDTO : fieldList) {
                if (fieldDTO!=null){
                    fieldMap.put(fieldDTO.typeName, fieldDTO);
                }
            }
            mapList.add(fieldMap);
        
        this.value = gson.toJsonTree(mapList);

    }
    /**
     * Set value to a list of compound fields (each member of the list is a set of fields)
     * @param compoundFieldList 
     */
    public void setMultipleCompound(List<HashSet<FieldDTO>> compoundFieldList) {
         Gson gson = new Gson();
        this.typeClass = "compound";
        this.multiple = true;
        List<Map<String, FieldDTO>> mapList = new ArrayList<Map<String, FieldDTO>>();
        for (Set<FieldDTO> compoundField : compoundFieldList) {
            Map<String, FieldDTO> fieldMap = new HashMap<>();
            for (FieldDTO fieldDTO : compoundField) {
                fieldMap.put(fieldDTO.typeName, fieldDTO);
            }
            mapList.add(fieldMap);
        }
        this.value = gson.toJsonTree(mapList);

    }

    /**
     *
     * @return the value of this field, translated from a JsonElement to the
     * appropriate DTO format
     */
    public Object getConvertedValue() {
        if (multiple) {
            if (typeClass.equals("compound")) {
                return getMultipleCompound();
            } else if (typeClass.equals("controlledVocabulary")){
                return getMultipleVocab();
            } else return getMultiplePrimitive();
            

        } else {
            if (typeClass.equals("compound")) {
                return getSingleCompound();
            } else if (typeClass.equals("controlledVocabulary")){
                return getSingleVocab();
            } else {
                return getSinglePrimitive();
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.typeName);
        hash = 53 * hash + Objects.hashCode(this.multiple);
        hash = 53 * hash + Objects.hashCode(this.typeClass);
        hash = 53 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldDTO other = (FieldDTO) obj;
        if (!Objects.equals(this.typeName, other.typeName)) {
            return false;
        }
        if (!Objects.equals(this.multiple, other.multiple)) {
            return false;
        }
        if (!Objects.equals(this.typeClass, other.typeClass)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FieldDTO{" + "typeName=" + typeName + ", multiple=" + multiple + ", typeClass=" + typeClass + ", value=" + getConvertedValue() + '}';
    }

}
