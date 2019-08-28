package banco;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entidade.Carteira;

public class Banco {
	
	public static boolean depositoCarteira(int numeroCarteira,  double valor) throws SQLException{
		BigDecimal saldo = new BigDecimal(getSaldoCarteira(numeroCarteira));
		BigDecimal soma = saldo.add(new BigDecimal(valor));
		 
		Connection conn = connect();
        String sql = "UPDATE carteira SET "
        		+ "saldo = ? "
                + "WHERE conta = ?";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
 
        // set the corresponding param
        pstmt.setDouble(1, soma.doubleValue());
        pstmt.setInt(2, numeroCarteira);
           
        // update 
        pstmt.executeUpdate();
        pstmt.close();
        conn.close();
        
        return pstmt.executeUpdate() >= 1 ? true : false;
    }
	
	public static boolean saqueCarteira(int numeroCarteira,  double valor) throws SQLException {
		BigDecimal saldo = new BigDecimal(getSaldoCarteira(numeroCarteira));
		BigDecimal soma = saldo.subtract(new BigDecimal(valor));
		 
		Connection conn = connect();
        String sql = "UPDATE carteira SET "
        		+ "saldo = ? "
                + "WHERE conta = ?";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
 
        // set the corresponding param
        pstmt.setDouble(1, soma.doubleValue());
        pstmt.setInt(2, numeroCarteira);
           
        // update 
        pstmt.executeUpdate();
        pstmt.close();
        conn.close();
        
        return pstmt.executeUpdate() >= 1 ? true : false;
    }
	
	public static boolean transferenciaCarteira(int numeroCarteiraOrigem, int numeroCarteiraDestino,  double valor) throws SQLException {
		
		if(!isSaldoTransferencia(numeroCarteiraOrigem, valor)){
			throw new SQLException("A conta ("+numeroCarteiraOrigem+") possui saldo insuficiente");
    	}
		
		BigDecimal saldo = new BigDecimal(getSaldoCarteira(numeroCarteiraOrigem));
		BigDecimal soma = saldo.subtract(new BigDecimal(valor));
		 
		Connection conn = connect();
		
		 // set auto-commit mode to false
        conn.setAutoCommit(false);
		
        String sql = "UPDATE carteira SET "
        		+ "saldo = ? "
                + "WHERE conta = ?";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setDouble(1, soma.doubleValue());
        pstmt.setInt(2, numeroCarteiraOrigem);
 
        pstmt.executeUpdate();
		
        saldo = new BigDecimal(getSaldoCarteira(numeroCarteiraDestino));
		soma = saldo.add(new BigDecimal(valor));
		
		PreparedStatement pstmt2 = conn.prepareStatement(sql);

		pstmt2.setDouble(1, soma.doubleValue());
		pstmt2.setInt(2, numeroCarteiraDestino);
 
		pstmt2.executeUpdate();
        
        conn.commit();
        
        pstmt.close();
        pstmt2.close();
        conn.close();
        
        return true;
    }
	
	public static Double getSaldoCarteira(int numeroCarteira) throws SQLException {
		Connection conn = connect();
		PreparedStatement stmt = conn.prepareStatement("select SALDO from carteira where conta = "+numeroCarteira);
		ResultSet resultSet = stmt.executeQuery();
		
		if(resultSet.isClosed())
			throw new SQLException("A conta ("+numeroCarteira+") não existe!");
		
		Double saldo = resultSet.getDouble("SALDO");
		stmt.close();
		conn.close();
		return saldo;
	}
	
	public static boolean isSaldoTransferencia(int numeroCarteira, double valor) throws SQLException{
		BigDecimal saldo = new BigDecimal(getSaldoCarteira(numeroCarteira));
		BigDecimal soma = saldo.subtract(new BigDecimal(valor));
		
		return soma.compareTo(new BigDecimal(0)) >= 0 ? true : false;
	}
	
	public static Carteira selectCarteira(int numeroCarteira) throws SQLException {
		Connection conn = connect();
		
		PreparedStatement stmt = conn.prepareStatement("select * from carteira where conta = "+numeroCarteira);
		ResultSet resultSet = stmt.executeQuery();
        
		if(resultSet.isClosed())
			throw new SQLException("A conta ("+numeroCarteira+") não existe!");
		
		Carteira carteira = new Carteira(
				resultSet.getInt("CONTA"), 
				resultSet.getString("NOME"), 
				resultSet.getDouble("SALDO"));
        
        stmt.close();
        conn.close();
        
        return carteira;
   }
	
   public static boolean criarCarteria(Carteira carteira) throws SQLException {
	   String sql = "INSERT INTO carteira( CONTA, NOME, SALDO) VALUES (?, ?, ?)";
	   Connection conn = connect();
	   PreparedStatement pstmt = conn.prepareStatement(sql);
       pstmt.setInt(1, carteira.getNumeroCarteira());
       pstmt.setString(2, carteira.getNome());
       pstmt.setDouble(3, carteira.getSaldo());
       return pstmt.executeUpdate() >= 1? true : false;
   }
	
	public static List<Carteira> selectCarteiras() throws SQLException {
		
		List<Carteira> carteiras = new ArrayList<Carteira>();
		
		Connection conn = connect();
		
		PreparedStatement stmt = conn.prepareStatement("select * from carteira");
        ResultSet resultSet = stmt.executeQuery();

        if(!resultSet.isClosed()) {
        	while (resultSet.next()) {
        		carteiras.add(new Carteira(
        				resultSet.getInt("CONTA"), 
        				resultSet.getString("NOME"), 
        				resultSet.getDouble("SALDO")));
        	}	
        }
		
       	stmt.close();
       	conn.close();        
        
        return carteiras;
    }
	
	public static Map<Integer,String> getConsultas() throws SQLException {
		
		Map<Integer,String> consultas = new HashMap<Integer, String>();
		
		Connection conn = connect();
		
		PreparedStatement stmt = conn.prepareStatement("select * from consultas");
        ResultSet resultSet = stmt.executeQuery();

        if(!resultSet.isClosed()) {
        	while (resultSet.next()) {
        		consultas.put(
        				resultSet.getInt("ID"), 
        				resultSet.getString("SQL"));
        	}	
        }
		
       	stmt.close();
       	conn.close();        
        
        return consultas;
    }
	
	public static int getIdUltimoRegistro() throws SQLException {
		
		Map<Integer,String> consultas = new HashMap<Integer, String>();
		
		Connection conn = connect();
		
		PreparedStatement stmt = conn.prepareStatement("select * from consultas");
        ResultSet resultSet = stmt.executeQuery();

        if(!resultSet.isClosed()) {
        	stmt.close();
           	conn.close();
        	return resultSet.getFetchSize();
        			
        }
        
        return 0;
		
    }
	
	public static boolean registrarConsulta(int id,String value) throws SQLException {
	   String sql = "INSERT INTO consultas( ID, SQL) VALUES (?, ?)";
	   Connection conn = connect();
	   PreparedStatement pstmt = conn.prepareStatement(sql);
	   pstmt.setInt(1, id);
	   pstmt.setString(2, value);
	   return pstmt.executeUpdate() >= 1 ? true : false;
	}
	
	public static boolean executarSql(String sql){
		Connection conn = connect();
		boolean result = false; 
		try {
			Statement stmt = conn.createStatement();
			result = stmt.executeUpdate(sql) == 1 ? true : false;
        	stmt.close();
        	conn.close();
        }catch (SQLException e) {
        	//e.printStackTrace();
        	return false; 
        }
        return result;
    }
	
	public static void creatTable() {
		 
		try {
			Statement statement = connect().createStatement();
	         statement.execute("CREATE TABLE IF NOT EXISTS carteira( CONTA INTEGER, NOME VARCHAR, SALDO REAL)");
	         statement.execute("CREATE TABLE IF NOT EXISTS consultas( ID INTEGER, SQL VARCHAR)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    private static Connection connect() {
        try  {
            return DriverManager.getConnection("jdbc:sqlite:banco.db");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }    
}