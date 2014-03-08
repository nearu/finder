package com.example.util;

/**
 * Created by gsl on 14-3-7.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

class Floor{
    int [][]data = new int[ConstData.max_lattice_length][ConstData.max_lattice_width];
    void clear(){
        for(int i = 0; i < ConstData.max_lattice_length; ++i)
            for(int j = 0; j < ConstData.max_lattice_width; ++j)
                data[i][j] = 0; // can go
    }
}
public class Building{

    int h = 0;
    String name;
    ArrayList <Floor> dataarr = new ArrayList<Floor>(); // array of floor  to construct the building
    List <String> namelist = new ArrayList<String>(); // search name at index
    Map <String, Integer> rev_namelist = new TreeMap<String, Integer>();// reverse

    double maxwidth = 0; // max double width
    double maxlength = 0; // max double length

    private static Floor parsefloor(Node node, Building ref){
        Floor ret = new Floor();
        NodeList childlist = node.getChildNodes();
        for(int i = 0; i < childlist.getLength(); ++i){
            Node polygon = childlist.item(i);
            String tag = polygon.getAttributes().getNamedItem("tag").getNodeValue();
            int tagid = ref.namelist.size(); // store in dataarr in floor
            if("forbid".equals(tag)){
                tagid = -1; // set tag for the position that is forbidden
            }
            else
            if("door".equals(tag)){
                tagid = -tagid - 2; // set tag for the door
            }
            else
                if("trans".equals(tag)){
                    tagid = -2; // set tag for transport
                }
            else{
                ref.namelist.add(polygon.getAttributes().getNamedItem("name").getNodeValue()); // add block name
                ref.rev_namelist.put(polygon.getAttributes().getNamedItem("name").getNodeValue(), tagid); // reverse search
            }
            List <Point> poly = new ArrayList<Point>();
            for(int j = 0; j < polygon.getChildNodes().getLength(); ++j){
                Node point = polygon.getChildNodes().item(j);
                Point tmp = new Point();
                tmp.x = Double.parseDouble(point.getAttributes().getNamedItem("x").getNodeValue());
                tmp.y = Double.parseDouble(point.getAttributes().getNamedItem("y").getNodeValue());
                poly.add(tmp);
            }
            for(int j = 0; j < ConstData.max_lattice_width; ++j)
                for(int k = 0; k < ConstData.max_lattice_length; ++k){
                    Point gridtoPoint = new Point(
                    (i + 0.5) * ref.maxwidth, (j + 0.5) * ref.maxlength);
                    if(Geometry.judge(gridtoPoint, poly)){
                       ret.data[i][j] = tagid;
                    }
                }
        }
        return ret;
    }
    public static Building initfromxml(String xml){
        String string = Helper.filetostring(xml);
        DocumentBuilder parse = null;
        try {
            parse = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println("init parse error");
            return null;
        }
        Document document = null;
        try {
            document = parse.parse(new ByteArrayInputStream(string.getBytes()));
        } catch (IOException e) {
            System.out.println("io exception");
            return null;
        }
        catch (SAXException e){
            System.out.println("SAXEeception");
        }
        Building ret = new Building();
        NodeList outerlist = document.getElementsByTagName("building");
        ret.name =  outerlist.item(0).getAttributes().getNamedItem("name").getNodeValue();
        ret.h = outerlist.item(0).getChildNodes().getLength();
        ret.maxlength = Double.parseDouble(outerlist.item(0).getAttributes().getNamedItem("maxlength").getNodeValue());
        ret.maxwidth = Double.parseDouble(outerlist.item(0).getAttributes().getNamedItem("maxwidth").getNodeValue());
        for(int i = 0; i < ret.h; ++i){
            Node item =  outerlist.item(0).getChildNodes().item(0);
            ret.dataarr.add(parsefloor(item, ret));
        }
        return ret;
    }
    public List<GridPoint3D> getpath (String start, String ed){
        int idenst = rev_namelist.get(start);
        int idened = rev_namelist.get(ed);
        GridPoint3D stpoint = null;
        GridPoint3D edpoint = null;
        for(int i = 0; i < h; ++i)
            for(int j = 0; j < ConstData.max_lattice_width; ++j)
                for(int k = 0; k < ConstData.max_lattice_length; ++k){
                    if(dataarr.get(i).data[j][k] == -idenst - 2){
                        stpoint = new GridPoint3D(i, j, k);
                    }
                    else
                    if(dataarr.get(i).data[j][k] == -idened - 2){
                        edpoint = new GridPoint3D(i, j , k);
                    }
                }
        try{
            return getpath(stpoint, edpoint);
        }
        catch(Exception e){
            System.out.println("getpath error");
            return null;
        }
    }
    public List <GridPoint3D> getpath (GridPoint3D st, GridPoint3D ed) throws Exception {
        int pathx[][][] = new int[ConstData.max_lattice_width][ConstData.max_lattice_length][h];
        int pathy[][][] = new int[ConstData.max_lattice_width][ConstData.max_lattice_length][h];
        int pathz[][][] = new int[ConstData.max_lattice_width][ConstData.max_lattice_length][h];
        boolean vi[][][] = new boolean [ConstData.max_lattice_width][ConstData.max_lattice_length][h];
        Queue <GridPoint3D> queue = new LinkedList<GridPoint3D>();
        queue.add(st);
        vi[st.x][st.y][st.z] = true;

        /// BFS to get the path
        while(!queue.isEmpty()){
            GridPoint3D pop = queue.remove();
            for(int i = 0; i < 8; ++i){
                int cx = Geometry.direct3D[i][0];
                int cy = Geometry.direct3D[i][1];
                int cz = Geometry.direct3D[i][2];
                GridPoint3D next = new GridPoint3D(pop.x + cx,pop.y + cy, pop.z + cz);
                if(vi[next.x][next.y][next.z]){
                    continue;   // have visit
                }
                if(cz != 0 && dataarr.get(pop.z).data[pop.x][pop.y] != -2){
                    continue;   // can't go up or down (except for the trans
                }
                if(dataarr.get(next.z).data[next.x][next.y] != -1){
                    pathx[next.x][next.y][next.z] = pop.x;
                    pathy[next.x][next.y][next.z] = pop.y;
                    pathz[next.x][next.y][next.z] = pop.z;
                    vi[next.x][next.y][next.z] = true; // tag visted....
                    queue.offer(next);// bfs next
                }
                else{
                    continue; // forbid
                }
            }
        }
        List <GridPoint3D> retpath = new ArrayList<GridPoint3D>();
        while(!ed.equals(st)){
            retpath.add(ed);
            ed = new GridPoint3D(pathx[ed.x][ed.y][ed.z], pathy[ed.x][ed.y][ed.z], pathz[ed.x][ed.y][ed.z]);
        }
        Collections.reverse(retpath);
        return retpath;
    }
}
