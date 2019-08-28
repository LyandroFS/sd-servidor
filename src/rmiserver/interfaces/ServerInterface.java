package rmiserver.interfaces;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import entidade.Carteira;

public interface ServerInterface extends java.rmi.Remote {
    void broadcastMessage(String message) throws RemoteException;
    void setLider(boolean isLider) throws RemoteException;
    boolean isLider() throws RemoteException;
    void setId(int id) throws RemoteException;
    int getId() throws RemoteException;
    void setNome(String nome) throws RemoteException;
    String getNome() throws RemoteException;
    int getIdUltimaOperaco() throws RemoteException;
    Map<Integer,String> getOperacoes() throws RemoteException;
    
    boolean depositoCarteira(int numeroCarteira,  double valor) throws RemoteException;
    boolean saqueCarteira(int numeroCarteira,  double valor) throws RemoteException;
    boolean transferenciaCarteira(int numeroCarteiraOrigem, int numeroCarteiraDestino,  double valor) throws RemoteException;
    Double getSaldoCarteira(int numeroCarteira) throws RemoteException;
    //boolean isSaldoTransferencia(int numeroCarteira, double valor) throws RemoteException;
    Carteira selectCarteira(int numeroCarteira) throws RemoteException;
    boolean criarCarteria(Carteira carteira) throws RemoteException;
    List<Carteira> selectCarteiras() throws RemoteException;
}