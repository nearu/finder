package com.example.util;

/**
 * Created by gsl on 14-3-7.
 */
public class ConstData{
    public static final int max_lattice_width = 1000;
    public static final int max_lattice_length = 1000;
    public static final int max_lattice_height = 10;
}
enum MapDataConst{
    CLASS,
    TOILET,
    STAIR,// to higher or lower
    FORBID,//non way
    CONNECT//TO other side
}