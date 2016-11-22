package weka_predictor;

public class TimeMeasurement {
	
	public long start_time_;
	public long end_time_;
	public long elapsed_sec_;
	
	public TimeMeasurement()
	{
		start_time_ = System.currentTimeMillis();
	}
	
	public void stopMeasure()
	{
		end_time_ = System.currentTimeMillis();
	}
	
	public String getEvolvedTime()
	{
		long time_diff = end_time_ - start_time_;
		elapsed_sec_ = time_diff/1000;
		return Long.toString(elapsed_sec_);
	}
}
