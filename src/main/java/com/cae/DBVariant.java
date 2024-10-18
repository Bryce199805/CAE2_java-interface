package com.cae;

// DBVariant 类用于存储不同类型的值
class DBVariant {
    private Object value;

    public DBVariant(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String asTypeString() {
        return (String) value;
    }

    public int asTypeInteger() {
        return (int) value;
    }

    public float asTypeFloat() {
        return (float) value;
    }

    public double asTypeDouble() {
        return (double) value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
