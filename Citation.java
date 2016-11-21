import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Citation {

  public static class CiteMap
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable cite = new IntWritable(1);
    private Text count = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	String [] valArray = value.toString().split("\t");
    	count.set(new Text(valArray[valArray.length-1]));
		context.write(count, cite);
    }
  }

  public static class CiteReduce
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
    Job job = Job.getInstance(conf, "Citation");
    job.setJarByClass(Citation.class);
    job.setMapperClass(CiteMap.class);
    job.setCombinerClass(CiteReduce.class);
    job.setReducerClass(CiteReduce.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
