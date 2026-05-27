package anthony.SuperCraftBrawl.playerdata;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ExecuteFunction {
	void execute(ResultSet set) throws SQLException;
}
