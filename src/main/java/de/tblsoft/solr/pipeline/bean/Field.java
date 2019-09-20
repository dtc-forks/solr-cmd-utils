package de.tblsoft.solr.pipeline.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tblsoft on 21.04.16.
 */
public class Field {

    public Field() {

    }

    public Field(String name, String value) {
        this.name = name;
        this.values = new ArrayList<String>();
        this.values.add(value);
    }

    public Field(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public Field(Field copy) {
        List<String> values = new ArrayList<>();
        if(copy.getValues() != null) {
            values = new ArrayList<>(copy.getValues());
        }

        this.name = copy.getName();
        this.values = values;
        this.datatype = copy.getDatatype();
    }

    private String name;

    private List<String> values;

    private Object rawValue;

    private String datatype;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getValue() {
        for(String value :getValues()) {
            return value;
        }
        return null;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", values=" + values +
                ", datatype='" + datatype + '\'' +
                '}';
    }

    public long getSize() {
        if(this.values == null) {
            return 0;
        }
        long totalSize = getName().length();
        for(String value: getValues()) {
            if(value != null) {
                totalSize = totalSize + value.length();
            }
        }

        return totalSize;
    }

    /**
     * Getter for property 'rawValue'.
     *
     * @return Value for property 'rawValue'.
     */
    public Object getRawValue() {
        return rawValue;
    }

    /**
     * Setter for property 'rawValue'.
     *
     * @param rawValue Value to set for property 'rawValue'.
     */
    public void setRawValue(Object rawValue) {
        this.rawValue = rawValue;
    }


    public boolean hasValues() {
        if(values != null && values.size() > 0) {
            return true;
        }

        if(this.rawValue != null) {
            return true;
        }

        return false;
    }
}
