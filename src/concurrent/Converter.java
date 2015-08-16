/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concurrent;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class for Outputs. Specified by project skeleton.
 */
class Converter implements ImageConvertible
{
    private final TDoubleArrayList values;

    public Converter(TDoubleArrayList values)
    {
        this.values = values;
    }

    /**
     * Returns the value of the node at the specified position.
     *
     * @param column the column of the desired node
     * @param row the row of the desired node
     * @return the value of the node at the specified position
     */
    @Override
    public double getValueAt(int column, int row)
    {
        return values.get(column * ConcOsmosis.getWidth() + row);
    }

    public void write2File(String path2file) {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < ConcOsmosis.getHeight(); ++row) {
            for (int column = 0; column < ConcOsmosis.getWidth(); ++column) {
                builder.append(getValueAt(column, row));
                builder.append(" ");
            }
            builder.append("\n");
        }
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path2file)))) {
            out.println(builder.toString());
        } catch (IOException e) {
        }
    }
}