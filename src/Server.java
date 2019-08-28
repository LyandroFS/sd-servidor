import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import banco.Banco;
import entidade.Carteira;
import rmiserver.interfaces.ServerInterface;


public class Server extends UnicastRemoteObject implements ServerInterface, Runnable {
    private static final long serialVersionUID = 1L;
    
    static Registry registry = null;
    static String nomeServer;
    static boolean isLider = false;
    static int id = 1;
    static int idUltimaOperacao = 0;
    static Map<Integer,String> operacoes;
 
    protected Server() throws RemoteException {
        Banco.creatTable();
        try {
			 idUltimaOperacao = Banco.getConsultas().size();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public synchronized void broadcastMessage(String message) throws RemoteException {
    	System.out.println("==============================================================");
    	
    	System.out.println("Servidor "+nomeServer);
    	System.out.println("Mensagem Recebida: "+ message);
    	
    	boolean errorSql = false;
    	if(!Banco.executarSql(message)) {
    		errorSql = true;
			throw new RemoteException("Não foi possível executar o SQL: "+message);
    	}	
    	
    	try {
    		if(!errorSql) Banco.registrarConsulta(this.idUltimaOperacao++, message);
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	if(this.isLider() && errorSql == false) {
    		Registry registry =  LocateRegistry.getRegistry(1099);
        	for(String servidor : registry.list()) {
        		try {
    				ServerInterface serverInterface = (ServerInterface) registry.lookup(servidor);
    				
    				if(!serverInterface.isLider()) {
    	     			serverInterface.broadcastMessage(message);
    					System.out.println("Replicando para: "+ serverInterface.getNome());
    				}	
    			} catch (NotBoundException e) {
    				e.printStackTrace();
    			}
         	}
    	}
    	
    	System.out.println("==============================================================");
    }
 
    public synchronized void sendMessageToClient(String message) throws RemoteException{}

    public static void main(String[] arg) throws RemoteException, MalformedURLException {
        
    	try{
    	    registry = LocateRegistry.createRegistry(1099);
    	    Server server = new Server();
    	    server.setLider(true);
    	    server.setId(1);
    	    server.setNome("BancoService1");
    	    Naming.rebind("BancoService1", server);
    	    
    	}catch (RemoteException e){
    	 
    	    try {
    	        registry = LocateRegistry.getRegistry(1099);
    	        String[] objects = registry.list();
    	        
    	        Server server = new Server();
    	        
    	        int id = objects.length + 1;
    	        //nomeServer = "BancoService"+id;
    	        nomeServer = "BancoService1";
    	        
    	        server.setId(id);
    	        server.setNome(nomeServer);
    	        
    	        Naming.rebind(nomeServer, server);
    	        //servidores.add(nomeServer);
    	        
    	    } catch (RemoteException ex) {
    	        ex.printStackTrace();
    	        System.out.println("RMI registry falha de conexão.");
    	        System.exit(1);
    	    }
    	}
    	
    	try {
 			operacoes = Banco.getConsultas();
 			ServerInterface server = (ServerInterface) registry.lookup(nomeServer);
			for(String nome : registry.list()){
	   			ServerInterface serverInterface = (ServerInterface) registry.lookup(nome);

	   			//Verifica se é o lider. Se for o lider, verifica se outros membros do grupo
	   			//precisam sincronizar as bases de dados
	   			if(serverInterface.isLider()) {
	   				if(idUltimaOperacao < serverInterface.getIdUltimaOperaco() ) {
	       				//System.out.println("Servidor "+serverInterface.getNome() +" precisam atualizar !"+serverInterface.getOperacoes().size());
	       				for(int x = idUltimaOperacao; x < serverInterface.getOperacoes().size() ; x ++) {
	   						//System.out.println("Executando: "+serverInterface.getOperacoes().get(x));
	   						server.broadcastMessage(serverInterface.getOperacoes().get(x));
	       				}
	       				Naming.rebind(nomeServer, server);
	       			}	
	   			}
	   		}
 		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	System.out.println("Serviço ativo..."+nomeServer);
    	new Thread(new Server()).start();
    }
    
    @Override
    public void run() {
    	
    	//Verifica se existe lider ativo, caso não existe elege um novo lider
    	 while(true) {
    		 
             try {
            	 boolean liderAtivo = false;
             	 registry =  LocateRegistry.getRegistry(1099);
             	
               	 for(String teste : registry.list()) {
             		ServerInterface serverInterface = (ServerInterface) registry.lookup(teste);
             		if(serverInterface.isLider()) 
             			liderAtivo = true;
                 }
               	 	
               	 if(!liderAtivo){
               	 	String nomeNovoLider = registry.list()[0];
               	 	ServerInterface serverInterface = (ServerInterface) registry.lookup(nomeNovoLider);
               	 	serverInterface.setLider(true);
               	 	Naming.rebind(nomeNovoLider, serverInterface);
             	 }
            	 
            	int r = (int) ((Math.random() * 2000) + 2000);
				Thread.sleep(r);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }	
    }
    

    @Override
   	public void setLider(boolean isLider) throws RemoteException {
   		this.isLider = isLider;
   	}
   
   @Override
  	public boolean isLider() throws RemoteException {
  		return this.isLider;
  	}

   @Override
   public void setId(int id) throws RemoteException {
	   this.id = id;
   }

   @Override
   public int getId() throws RemoteException {
	   return this.id;
   }

	@Override
	public void setNome(String nome) throws RemoteException {
		this.nomeServer = nome;
		
	}

	@Override
	public String getNome() throws RemoteException {
		// TODO Auto-generated method stub
		return this.nomeServer;
	}

	@Override
	public int getIdUltimaOperaco() throws RemoteException {
		return this.idUltimaOperacao;
	}

	@Override
	public Map<Integer, String> getOperacoes() throws RemoteException {
		try {
			return Banco.getConsultas();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public boolean depositoCarteira(int numeroCarteira, double valor) throws RemoteException {
		try {
			return Banco.depositoCarteira(numeroCarteira, valor);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public boolean saqueCarteira(int numeroCarteira, double valor) throws RemoteException {
		try {
			return Banco.saqueCarteira(numeroCarteira, valor);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public boolean transferenciaCarteira(int numeroCarteiraOrigem, int numeroCarteiraDestino, double valor)
			throws RemoteException {
		try {
			return Banco.transferenciaCarteira(numeroCarteiraOrigem, numeroCarteiraDestino, valor);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public Double getSaldoCarteira(int numeroCarteira) throws RemoteException {
		try {
			return Banco.getSaldoCarteira(numeroCarteira);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	/*@Override
	public boolean isSaldoTransferencia(int numeroCarteira, double valor) throws RemoteException {
		try {
			return Banco.isSaldoTransferencia(numeroCarteira, valor);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}*/

	@Override
	public Carteira selectCarteira(int numeroCarteira) throws RemoteException {
		try {
			return Banco.selectCarteira(numeroCarteira);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public boolean criarCarteria(Carteira carteira) throws RemoteException {
		try {
			return Banco.criarCarteria(carteira);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public List<Carteira> selectCarteiras() throws RemoteException {
		try {
			return Banco.selectCarteiras();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	} 
}