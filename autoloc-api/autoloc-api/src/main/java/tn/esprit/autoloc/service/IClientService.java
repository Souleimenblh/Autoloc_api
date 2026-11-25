package tn.esprit.autoloc.service;

import tn.esprit.autoloc.entity.Client;

import java.util.List;

// Service contract: declares the operations available to controllers.
public interface IClientService {
    Client addClient(Client client);
    Client updateClient(Client client);
    Client retrieveClient(Long idClient);
    List<Client> retrieveAllClients();
    void deleteClient(Long idClient);
}

