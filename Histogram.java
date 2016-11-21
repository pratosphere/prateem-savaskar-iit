
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Histogram {

  public static class HistoMap
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable cite = new IntWritable(1);
    private Text count = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	String [] valArray = value.toString().split(",");
    	count.set(new Text(valArray[valArray.length-1]));
		context.write(count, cite);
    }
  }

  public static class HistoReduce
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "count count");
    job.setJarByClass(Histogram.class);
    job.setMapperClass(HistoMap.class);
    job.setCombinerClass(HistoReduce.class);
    job.setReducerClass(HistoReduce.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
