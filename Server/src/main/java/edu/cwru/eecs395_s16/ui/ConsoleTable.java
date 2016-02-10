package edu.cwru.eecs395_s16.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by james on 2/9/16.
 */
public class ConsoleTable {
    List<String> rowHeaders;
    List<List<Object>> data;
    int totalCols = 0;
    int totalColsData = 0;
    public ConsoleTable() {
        data = new ArrayList<>();
    }

    public void setRowHeaders(String... headers){
        this.rowHeaders = new ArrayList<>(Arrays.asList(headers));
        totalCols = Math.max(headers.length,totalColsData);
    }

    public void addRow(Object... row){
        this.data.add(new ArrayList<>(Arrays.asList(row)));
        this.totalColsData = Math.max(totalColsData,row.length);
        this.totalCols = Math.max(totalCols,totalColsData);
    }

    @Override
    public String toString() {
        //Create array of individual column sizes
        int[] sizes = new int[totalCols];

        //Pad the headers and data
        while(rowHeaders.size() < totalCols){
            rowHeaders.add("");
        }
        for(List<Object> row : data){
            while(row.size() < totalCols){
                row.add("");
            }
        }

        //Compute individual column sizes
        for(int i=0; i<rowHeaders.size(); i++){
            sizes[i] = Math.max(sizes[i],rowHeaders.get(i).length());
        }
        for(List<?> row : data){
            for(int i=0; i<row.size(); i++){
                sizes[i] = Math.max(sizes[i],row.get(i).toString().length());
            }
            for(int i=row.size(); i<totalCols; i++){
                sizes[i] = Math.max(sizes[i],1);
            }
        }

        //Build format string
        StringBuilder s = new StringBuilder();
        int totalSpaces = 0 ;
        for(int i=0; i<totalCols; i++){
            s.append("%").append("-").append(sizes[i]).append("s | ");
            totalSpaces += sizes[i] + 3;
        }
        s.append("%n");
        String formatStr = s.toString();

        //Reset StringBuilder to build each line.
        s.setLength(0);
        Object[] tempArray = new Object[0];
        s.append(String.format(formatStr,rowHeaders.toArray(tempArray)));
        for(int i=0; i<totalSpaces; i++){
            s.append("-");
        }
        s.append(String.format("%n"));
        for(List<Object> row : data){
            s.append(String.format(formatStr,row.toArray(tempArray)));
        }
        return s.toString();
    }
}
