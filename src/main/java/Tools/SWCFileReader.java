/**
 * 
 */
package Tools;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brahim AMAIRI
 * @created 15 03 2010 
 *
 */
public class SWCFileReader {

	private List<Swc> listSwc = new ArrayList<Swc>();

	public SWCFileReader(String path) {
		File fileSWC=null;
		fileSWC = new File(path);
		if(fileSWC!=null){
			try {

				BufferedReader in1SWC = new BufferedReader(new FileReader(fileSWC));
				BufferedReader inSWC = new BufferedReader(new FileReader(fileSWC));
				LineNumberReader lnr = new LineNumberReader(in1SWC);
				int NlineSWC = 0;
				String lineSWC=null;
				String[] dataSWC;
				while(lnr.readLine()!=null){
					NlineSWC=lnr.getLineNumber();
				}
				for(int i=0 ;i<NlineSWC ; i++){
					lineSWC = inSWC.readLine();
					if(lineSWC!=null){
						if(!lineSWC.startsWith("#") & lineSWC.length()!=0){
							lineSWC = lineSWC.replaceAll("\t", " ");
							dataSWC =lineSWC.split(" ");
							int type = Integer.parseInt(dataSWC[1]);
							double x = Double.parseDouble(dataSWC[2]);
							double y = Double.parseDouble(dataSWC[3]);
							double z = Double.parseDouble(dataSWC[4]);
							double r = Double.parseDouble(dataSWC[5]);
							listSwc.add(new Swc(x, y, z, r,type));
						}
					}

				}
				in1SWC.close();
				inSWC.close();
			}catch (IOException e) {
				IJ.error(e.getMessage());
			}

		}


	}

	public List<Swc> getListSwc() {
		return listSwc;
	}


}
