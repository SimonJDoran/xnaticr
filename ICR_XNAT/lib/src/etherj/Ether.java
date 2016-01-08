/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj;

import java.io.File;

/**
 *
 * @author jamesd
 */
public class Ether
{
	public static String getEtherDir()
	{
		return System.getProperty("user.home")+File.separator+".ether"+
			File.separator;
	}
}
