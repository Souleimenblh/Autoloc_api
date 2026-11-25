package tn.esprit.autoloc.service;

import tn.esprit.autoloc.entity.Equipement;

import java.util.List;

// Service contract: declares the operations available to controllers.
public interface IEquipementService {
    Equipement addEquipement(Equipement equipement);
    Equipement updateEquipement(Equipement equipement);
    Equipement retrieveEquipement(Long idEquipement);
    List<Equipement> retrieveAllEquipements();
    void deleteEquipement(Long idEquipement);
}

