/*
 * @(#)DynamicCharacterArray.java	1.3 02/08/26 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */
package com.sun.midp.lcdui;

/**
 * Similiar to StringBuffer but does NOT resize when needed, only when
 * requested
 */
public class DynamicCharacterArray {

    /**
     * buffer to store data. The capacity of this DynamicCharacterArray
     * is equal to the length of this buffer
     */
    protected char buffer[];

    /** 
     * the length of the array currently in use
     */
    protected int length;

    /**
     * Initializes the DCA with a capacity and an empty array
     *
     * @param capacity the maximum storage capacity
     * @throws IllegalArgumentException if capacity <= 0 
     * @throws IllegalArgumentException if data.length > capacity 
     */
    public DynamicCharacterArray(int capacity) {
        this(null, capacity);
    }

    /**
     * Initializes the array with str and a capacity of str.length()
     *
     * @param str initial array data, may NOT be null
     * @throws NullPointerException if str is null
     */
    public DynamicCharacterArray(String str) {
        this(str.toCharArray());
    }

    /**
     * Initializes the array with data and a capacity of data.length
     *
     * @param data initial array data, may NOT be null
     * @throws NullPointerException if data is null
     */
    public DynamicCharacterArray(char[] data) {
        this(data, data.length);
    }

    /**
     * Initializes the array with data and a capacity of capacity
     *
     * @param data initial array data, may be null
     * @param capacity initial maximum capacity
     * @throws IllegalArgumentException if capacity <= 0 
     * @throws IllegalArgumentException if data.length > capacity 
     */
    public DynamicCharacterArray(char[] data, int capacity) {
        int len;

        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        if (data != null) {
            if (data.length > capacity) {
                throw new IllegalArgumentException();
            }

            this.length = data.length;

            buffer = new char[capacity];
            System.arraycopy(data, 0, buffer, 0, this.length);

        } else {
            buffer = new char[capacity];
        }
    }

    /**
     * Inserts an subset of an array into the buffer.
     * The offset parameter must be withint the range [0..(size())], inclusive.
     * The length parameter must be a non-negative integer such that 
     * (offset + length) <= size().
     *
     * @param data array to insert
     * @param offset offset into data
     * @param length length of subset
     * @param position index into the internal buffer to insert the subset
     * @return the actual position the data was inserted
     * @throws ArrayIndexOutOfBoundsException if offset and length
     *         specify an invalid range
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     * @throws NullPointerException if data is null
     */
    public int insert(char[] data, int offset, int length, int position) {

        if (position < 0) {
            position = 0;
        } else if (position > this.length) {
            position = this.length;
        }

        if (offset < 0 
            || offset > data.length 
            || length < 0
            || length > data.length
            || (offset + length) < 0
            || (offset + length) > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (this.length + length > buffer.length) {
            throw new IllegalArgumentException();
        }

        if (data.length != 0) {

            System.arraycopy(buffer, position, 
                             buffer, position + length, 
                             this.length - position);
            System.arraycopy(data, offset, buffer, position, length);

            this.length += length;
        }

        return position;
    }

    /**
     * Inserts an character into the buffer.
     *
     * @param ch character to insert
     * @param position index into the internal buffer to insert the subset
     * @return the actual position the data was inserted
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     */
    public int insert(int position, char ch) {
        char arr[] = { ch };
        return insert(arr, 0, 1, position);
    }
   
    /**
     * Inserts an String into the buffer.
     *
     * @param str Strimg to insert
     * @param position index into the internal buffer to insert the subset
     * @return the actual position the data was inserted
     * @throws ArrayIndexOutOfBoundsException if offset and length
     *         specify an invalid range
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     * @throws NullPointerException if data is null
     */
    public int insert(int position, String str) {
        return insert(str.toCharArray(), 0, str.length(), position);
    }

    /**
     * Appends a character onto the end of the buffer.
     *
     * @param c character to append
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     */
    public void append(char c) {
        insert(length, c);
    }

    /**
     * Sets the internal buffer to the values of the subset specified
     * The offset parameter must be withint the range [0..(size())], inclusive.
     * The length parameter must be a non-negative integer such that 
     * (offset + length) <= size(). If data is null the buffer is emptied.
     *
     * @param data the data to use to set the buffer
     * @param offset offset into data
     * @param length length of the subset
     * @throws ArrayIndexOutOfBoundsException if offset and length
     *         specify an invalid range
     * @throws IllegalArgumentException if length exceeds the capacity
     */
    public void set(char[] data, int offset, int length) {

        if (data == null) {
            this.length = 0;
            return;
        }

        if (offset < 0 
            || offset > data.length 
            || length < 0
            || length > data.length
            || (offset + length) < 0
            || (offset + length) > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (length > buffer.length) { 
            throw new IllegalArgumentException();
        }

        System.arraycopy(data, offset, buffer, 0, length);
        this.length = length;
    }

    /**
     * Returns the internal buffer in the array provided. This must
     * be at least length() long.
     *
     * @param data array to fill with the character of the internal buffer
     * @throws IndexOutOfBoundsException if data cannot hold the contents
     * @throws NullPointerException if data is null
     */
    public void get(char[] data) {
        getChars(0, buffer.length, data, 0);
    }

    /**
     * Returns the internal buffer in the array provided. 
     *
     * @param position index into the internal buffer to start copying
     * @param length length of region to copy
     * @param data array to fill with the character of the internal buffer
     * @param offset offset into data to copy to
     * @throws IndexOutOfBoundsException if data cannot hold the contents
     * @throws NullPointerException if data is null
     */
    public void getChars(int position, int length, char[] data, int offset) {
        System.arraycopy(buffer, position, data, offset, length);
    }

    /**
     * Returns a copy of the active portion of the internal buffer. 
     *
     * @return character array
     */
    public char[] toCharArray() {
        char[] buf = new char[length];
        System.arraycopy(buffer, 0, buf, 0, buf.length); 
        return buf;
    }

    /**
     * Deletes the sepecified range from the internal buffer
     *
     * @param offset offset to begin deleting
     * @param length length of portion to delete
     * @throws StringIndexOutOfBoundsException if offset and length do
     *         not specifiy a valid range in the internal buffer
     */
    public void delete(int offset, int length) {
        if (offset < 0 
            || length < 0 
            || (offset + length) < 0
            || (offset + length) > this.length) {
            throw new StringIndexOutOfBoundsException();
        }

        if ((offset + length) < this.length) {
            System.arraycopy(buffer, offset + length, 
                             buffer, offset, 
                             this.length - (offset + length));
        }

        this.length -= length;
    }

    /**
     * Sets the maximum capacity to the specified value. the buffer may
     * be truncated if the new capacity is less than the current capacity.
     *
     * @param capacity new maximum capacity
     * @throws IllegalArgumentException is zero or less
     */

    public void setCapacity(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        if (buffer.length == capacity) {
            return;
        }

        if (this.length > capacity) {
            this.length = capacity;
        }

        char[] newBuffer = new char[capacity];
        System.arraycopy(buffer, 0, newBuffer, 0, this.length);
 
        buffer = newBuffer;
    }

    /**
     * Returns the current capacity
     *
     * @return the maximum capacity 
     */
    public int capacity() {
        return buffer.length;
    }

    /**  
     * Returns the length of current data
     *
     * @return current length
     */ 
    public int length() {
        return this.length;
    }

    /**
     * Returns the character at the specified index of the internal buffer
     *
     * @param index index into the buffer
     * @return the character at the specified index
     * @throws IndexOutOfBoundsException if the 0 < index or index > length()
     */
    public char charAt(int index) {
        if (index < 0 || index > this.length) { 
            throw new IndexOutOfBoundsException();
        }

        return buffer[index];
    }

    /**
     * Sets the character at index to ch
     *
     * @param index index into the buffer
     * @param ch character to set to
     * @throws IndexOutOfBoundsException if the 0 < index or index > length()
     */
    public void setCharAt(int index, char ch) {
        if (index < 0 || index > this.length) { 
            throw new IndexOutOfBoundsException();
        }

        buffer[index] = ch;
    }

    /**
     * Return a String representation of the internal buffer
     *
     * @return a String object
     */
    public String toString() {
        return String.valueOf(buffer, 0, this.length);
    }
}

