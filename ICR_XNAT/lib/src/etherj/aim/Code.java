/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Displayable;

/**
 *
 * @author jamesd
 */
public class Code implements Displayable
{
	private String code = "";
	private String codeSystem = "";
	private String codeSystemName = "";
	private String codeSystemVersion = "";

	@Override
	public void display()
	{
		display("", false);
	}

	@Override
	public void display(boolean recurse)
	{
		display("", recurse);
	}

	@Override
	public void display(String indent)
	{
		display(indent, false);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		System.out.println(pad+"Code: "+code);
		System.out.println(pad+"CodeSystem: "+codeSystem);
		System.out.println(pad+"CodeSystemName: "+codeSystemName);
		System.out.println(pad+"CodeSystemVersion: "+codeSystemVersion);
	}

	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @return the codeSystem
	 */
	public String getCodeSystem()
	{
		return codeSystem;
	}

	/**
	 * @return the codeSystemName
	 */
	public String getCodeSystemName()
	{
		return codeSystemName;
	}

	/**
	 * @return the codeSystemVersion
	 */
	public String getCodeSystemVersion()
	{
		return codeSystemVersion;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code)
	{
		this.code = code;
	}

	/**
	 * @param codeSystem the codeSystem to set
	 */
	public void setCodeSystem(String codeSystem)
	{
		this.codeSystem = codeSystem;
	}

	/**
	 * @param codeSystemName the codeSystemName to set
	 */
	public void setCodeSystemName(String codeSystemName)
	{
		this.codeSystemName = codeSystemName;
	}

	/**
	 * @param codeSystemVersion the codeSystemVersion to set
	 */
	public void setCodeSystemVersion(String codeSystemVersion)
	{
		this.codeSystemVersion = codeSystemVersion;
	}
}
