/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Ether;
import etherj.db.DatabaseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jamesd
 */
public class AimToolkit
{
	private static final String Default = "default";
	private static final Logger logger = LoggerFactory.getLogger(AimToolkit.class);
	private static final Map<String,AimToolkit> toolkitMap = new HashMap<>();

	static
	{
		toolkitMap.put(Default, new AimToolkit());
	}

	/**
	 *
	 * @return
	 */
	public static AimToolkit getDefaultToolkit()
	{
		return getToolkit(Default);
	}

	/**
	 *
	 * @return
	 */
	public static AimToolkit getToolkit()
	{
		return getToolkit(Default);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public static AimToolkit getToolkit(String key)
	{
		return toolkitMap.get(key);
	}

	/**
	 *
	 * @param key
	 * @param toolkit
	 * @return
	 */
	public static AimToolkit setToolkit(String key, AimToolkit toolkit)
	{
		AimToolkit tk = toolkitMap.put(key, toolkit);
		logger.info(toolkit.getClass().getName()+" set with key '"+key+"'");
		return tk;
	}

	/**
	 *
	 * @return
	 * @throws DatabaseException
	 */
	public AimDatabase createAimDatabase() throws DatabaseException
	{
		return new SqliteAimDatabase(Ether.getEtherDir()+"aim.db");
	}

	/**
	 *
	 * @param properties
	 * @return
	 * @throws etherj.db.DatabaseException
	 */
	public AimDatabase createAimDatabase(Properties properties) throws DatabaseException
	{
		String path = properties.getProperty("db.filename", "aim.db");
		return new SqliteAimDatabase(path);
	}
}
