package weka_predictor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CsvWriter
{
	public FileWriter csv_writer_;
	String csv_output_file_path_;

	public CsvWriter(String csv_output_file_path) throws IOException
	{
		csv_output_file_path_ = csv_output_file_path;
		
		csv_writer_ = new FileWriter(csv_output_file_path_);
		
		PerfReport.writeCSVHeader(csv_writer_);
		csv_writer_.write(System.getProperty( "line.separator" ));
	}

	//-----------------------------------------------------------------
	public void writeToCsv(ArrayList<PerfReport> all_reports) throws IOException
	{
		for (PerfReport cur_report: all_reports)
		{
			cur_report.writeCsv(csv_writer_);
			csv_writer_.write(System.getProperty( "line.separator" ));
		}
		csv_writer_.flush();
	}
	
	//-----------------------------------------------------------------
	private void closeCsv() throws IOException
	{
		csv_writer_.flush();
		csv_writer_.close();
		System.out.println("+++ " + csv_output_file_path_ +  " closed.");
	}
	
	//-----------------------------------------------------------------
	protected void finalize() throws Throwable {
		closeCsv();
	}

}