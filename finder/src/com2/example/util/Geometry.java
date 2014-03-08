package com.example.util;
import java.util.ArrayList;
import java.util.List;

class Point{
    double x = 0;
    double y = 0;
    Point(){

    }
    Point(double x, double y){
        this.x = x;
        this.y = y;
    }

}
class GridPoint3D{
    int x = 0;
    int y = 0;
    int z = 0;
    GridPoint3D(){

    }
    GridPoint3D(int x, int y, int z){
        this.x = x;
        this.z = z;
        this.y = y;
    }
    boolean equals(GridPoint3D a){
        return  x == a.x && y == a.y && z == a.z;
    }
    boolean belong(int x, int y, int z){
        return this.x < x && this.y < y && this.z < z;
    }
}
public class Geometry {
    public static double eps = 1e-6;
    public static int direct3D[][] = {
            {0,0,-1},
            {0,0,1},
            {1,0,0},
            {-1,0,0},
            {0,1,0},
            {0,-1,0}
    };
    public static boolean judge(Point p, List <Point> polygon){
        int l = polygon.size();
        int totalinsersect = 0; //num of intersect points
        for(int i = 0; i < l; ++i){
            Point first = polygon.get(i);
            Point second = polygon.get((i + 1) % l);
            double k = (first.y - second.y) / (first.x - second.x);
            if(Math.abs(k) < eps)
                continue;
            if ( (k > 0 && p.y > first.y && p.y < second.y) || (k < 0 && p.y > second.y && p.y < first.y) ){ // judge intersect in the inner
                ++totalinsersect;
                continue;
            }
            if(p.y == first.y && k > 0)
                ++totalinsersect;
            if(p.y == second.y && k < 0)
                ++totalinsersect;
        }
        return totalinsersect % 2 == 1;
    }
}
