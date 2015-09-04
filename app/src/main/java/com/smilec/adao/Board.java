package com.smilec.adao;

/**
 * Created by sxcui on 2015/8/31.
 */
public class Board
{
    public String Name,Msg;
    public int Id;
    public int Fgroup;
    public Board(int id,String name,int fgroup,String msg)
    {
        Name = name;
        Id = id;
        Fgroup = fgroup;
        Msg = msg;
    }
}