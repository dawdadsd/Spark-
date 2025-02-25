package sparkanalysis.schema;

import org.apache.spark.sql.types.*;

public class JingcaiSchema {
    public static final StructType schema = new StructType(new StructField[]{
        new StructField("ticket_number", DataTypes.StringType, false, Metadata.empty()),
        new StructField("province", DataTypes.StringType, true, Metadata.empty()),
        new StructField("city", DataTypes.StringType, true, Metadata.empty()),
        new StructField("store_id", DataTypes.StringType, true, Metadata.empty()),
        new StructField("terminal_id", DataTypes.StringType, true, Metadata.empty()),
        new StructField("sale_date", DataTypes.DateType, true, Metadata.empty()),
        new StructField("sale_time", DataTypes.TimestampType, true, Metadata.empty()),
        new StructField("sport_type", DataTypes.StringType, true, Metadata.empty()),
        new StructField("pass_type", DataTypes.StringType, true, Metadata.empty()),
        new StructField("game_type", DataTypes.StringType, true, Metadata.empty()),
        new StructField("amount", DataTypes.DoubleType, true, Metadata.empty()),
        new StructField("bet_content", DataTypes.StringType, true, Metadata.empty()),
        new StructField("multiple", DataTypes.IntegerType, true, Metadata.empty())
    });
}
