package com.wandoujia.hbase;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;

public class HBaseUtil {

    /**
     * @param startKey
     * @param endKey
     * @param numRegions
     * @return
     */
    public static byte[][] getHexSplits(String startKey, String endKey,
            int numRegions) {
        byte[][] splits = new byte[numRegions - 1][];
        BigInteger lowestKey = new BigInteger(startKey, 16);
        BigInteger highestKey = new BigInteger(endKey, 16);
        BigInteger range = highestKey.subtract(lowestKey);
        BigInteger regionIncrement = range.divide(BigInteger
                .valueOf(numRegions));
        lowestKey = lowestKey.add(regionIncrement);
        for (int i = 0; i < numRegions - 1; i++) {
            BigInteger key = lowestKey.add(regionIncrement.multiply(BigInteger
                    .valueOf(i)));
            byte[] b = String.format("%016x", key).getBytes();
            splits[i] = b;
        }
        return splits;
    }

    /**
     * @param tableName
     * @param familyName
     * @param bloomType
     * @param compressionType
     * @param inMemory
     * @param blockCacheEnabled
     * @param blockSize
     * @param maxVersions
     * @return
     */
    public static HTableDescriptor generateTableDescriptor(String tableName,
            byte[] familyName, BloomType bloomType, Algorithm compressionType,
            Boolean inMemory, Boolean blockCacheEnabled, int blockSize,
            int maxVersions) {
        HTableDescriptor table = new HTableDescriptor(tableName);
        HColumnDescriptor hcd = new HColumnDescriptor(familyName);
        hcd.setBloomFilterType(bloomType);
        hcd.setCompressionType(compressionType);
        hcd.setInMemory(inMemory);
        hcd.setBlockCacheEnabled(blockCacheEnabled);
        hcd.setBlocksize(blockSize);
        hcd.setMaxVersions(maxVersions);
        // other descriptor
        hcd.setScope(0);
        hcd.setTimeToLive(-1);
        table.addFamily(hcd);
        return table;
    }

    /**
     * @param tableName
     * @param familyName
     * @param bloomType
     * @param compressionType
     * @param inMemory
     * @param blockCacheEnabled
     * @param blockSize
     * @param maxVersions
     * @return
     */
    public static HTableDescriptor getTableDescriptor(String tableName,
            String familyName, BloomType bloomType, Algorithm compressionType,
            Boolean inMemory, Boolean blockCacheEnabled, int blockSize,
            int maxVersions) {
        HTableDescriptor table = new HTableDescriptor(tableName);
        HColumnDescriptor hcd = new HColumnDescriptor(familyName);
        hcd.setBloomFilterType(bloomType);
        hcd.setCompressionType(compressionType);
        hcd.setInMemory(inMemory);
        hcd.setBlockCacheEnabled(blockCacheEnabled);
        hcd.setBlocksize(blockSize);
        hcd.setMaxVersions(maxVersions);

        // other descriptor
        hcd.setScope(0);
        hcd.setTimeToLive(-1);
        table.addFamily(hcd);
        return table;
    }

    /**
     * @param familyName
     * @param column
     * @param compareOp
     * @param value
     * @param filterIfMissing
     * @return
     */
    public static Filter getFilter(byte[] familyName, byte[] column,
            CompareOp compareOp, byte[] value, boolean filterIfMissing) {
        SingleColumnValueFilter scvf = new SingleColumnValueFilter(familyName,
                column, compareOp, value);
        scvf.setFilterIfMissing(filterIfMissing);
        scvf.setLatestVersionOnly(true);
        return scvf;
    }

    /**
     * @param familyName
     * @param columns
     * @param compareOps
     * @param values
     * @param operator
     * @param filterIfMissing
     * @return
     */
    public static Filter getFilter(byte[] familyName, List<byte[]> columns,
            List<CompareOp> compareOps, List<byte[]> values, Operator operator,
            boolean filterIfMissing) {
        if (columns.size() != compareOps.size()
                || compareOps.size() != values.size()) {
            return null;
        }
        FilterList filter = new FilterList(operator);
        for (int i = 0; i < columns.size(); i++) {
            SingleColumnValueFilter scvf = new SingleColumnValueFilter(
                    familyName, columns.get(i), compareOps.get(i),
                    values.get(i));
            scvf.setFilterIfMissing(filterIfMissing);
            scvf.setLatestVersionOnly(true);
            filter.addFilter(scvf);
        }
        return filter;
    }

    /**
     * @param result
     * @param withRowKey
     * @return
     */
    public static Map<String, String> getRowColumns(Result result,
            boolean withRowKey) {
        if (result == null || result.size() < 1) {
            return null;
        }
        Map<String, String> rowColumns = new HashMap<String, String>();
        if (withRowKey == true) {
            // put row_key
            rowColumns.put(Constants.ROW_KEY, new String(result.getRow()));
        }
        // put columns
        for (KeyValue kv: result.list()) {
            rowColumns.put(new String(kv.getQualifier()),
                    new String(kv.getValue()));
        }
        return rowColumns;
    }
}
