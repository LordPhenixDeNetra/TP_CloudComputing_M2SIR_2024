package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ProjetCloudTPCloudSIM {

    private static final int NUMBER_OF_DATACENTERS = 1;
    private static final int HOST_MIPS = 1000;
    private static final int HOST_RAM = 2048; // Mémoire RAM de l'hôte en MB
    private static final long HOST_STORAGE = 1000000; // Stockage de l'hôte en MB
    private static final int HOST_BW = 10000; // Bande passante de l'hôte en Mbps

    public static void main(String[] args) {
        Log.printLine("Démarrage de la simulation CloudSim...");

        try {

            // Scénarios de simulation : nombre de requêtes par seconde
            int[] requestsPerSecond = {200, 500, 1000, 2000};

//            int[] requestsPerSecond = {20, 50, 100, 200};

            for (int requests : requestsPerSecond) {

                // Initialiser CloudSim pour chaque scénario
                int num_user = 1; // Nombre d'utilisateurs simulés
                Calendar calendar = Calendar.getInstance(); // Date de début de la simulation
                boolean trace_flag = false; // Drapeau pour le suivi des événements de la simulation
                CloudSim.init(num_user, calendar, trace_flag);

                // Créer des centres de données (Datacenters)
                Datacenter datacenter0 = createDatacenter("Datacenter_0");

                // Créer un courtier (Broker) pour gérer les VMs et Cloudlets
                DatacenterBroker broker = createBroker();
                int brokerId = broker.getId();

                // Configuration des VMs et des Cloudlets
                int vmMemory = 512; // Mémoire de la VM en MB
                long vmSize = 10000; // Taille de la VM en MB
                int vmBw = 1000; // Bande passante de la VM en Mbps
                int vmPesNumber = 1; // Nombre de PEs (Processing Elements) pour la VM
                String vmm = "Xen"; // VMM (Virtual Machine Monitor) utilisé

                // Créer une liste de VMs
                List<Vm> vmList = new ArrayList<>();
                for (int i = 0; i < 5; i++) { // Débuter avec 5 VMs
                    Vm vm = new Vm(i, brokerId, HOST_MIPS, vmPesNumber, vmMemory, vmBw, vmSize, vmm, new CloudletSchedulerTimeShared());
                    vmList.add(vm);
                }

                // Soumettre la liste des VMs au broker
                broker.submitVmList(vmList);

                // Configuration des Cloudlets
                int cloudletLength = 1000; // Longueur de la Cloudlet
                int cloudletPesNumber = 1; // Nombre de PEs pour la Cloudlet
                long cloudletFileSize = 300; // Taille du fichier de la Cloudlet
                long cloudletOutputSize = 300; // Taille de sortie de la Cloudlet
                UtilizationModel utilizationModel = new UtilizationModelFull(); // Modèle d'utilisation des ressources

                // Créer une liste de Cloudlets
                List<Cloudlet> cloudletList = new ArrayList<>();
                for (int i = 0; i < requests; i++) {
                    Cloudlet cloudlet = new Cloudlet(i, cloudletLength, cloudletPesNumber,
                            cloudletFileSize, cloudletOutputSize, utilizationModel, utilizationModel, utilizationModel);
                    cloudlet.setUserId(brokerId);
                    cloudletList.add(cloudlet);
                }

                // Soumettre la liste des Cloudlets au broker
                broker.submitCloudletList(cloudletList);

                // Démarrer la simulation
                CloudSim.startSimulation();

                // Arrêter la simulation
                CloudSim.stopSimulation();

                // Afficher les résultats
                List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
                printCloudletList(finishedCloudlets);

                // Calculer le temps de réponse moyen
                double totalResponseTime = 0;
                for (Cloudlet cloudlet : finishedCloudlets) {
                    totalResponseTime += cloudlet.getFinishTime() - cloudlet.getSubmissionTime();
                }
                double averageResponseTime = totalResponseTime / finishedCloudlets.size();

                Log.printLine("Temps de réponse moyen pour " + requests + " requêtes/seconde: " + averageResponseTime + " secondes");
            }

            Log.printLine("Simulation CloudSim terminée !");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("La simulation a été interrompue en raison d'une erreur inattendue");
        }
    }

    // Méthode pour créer un centre de données (Datacenter)
    private static Datacenter createDatacenter(String name) {
        // Liste des hôtes dans le centre de données
        List<Host> hostList = new ArrayList<>();
        // Liste des unités de traitement (PEs) de l'hôte
        List<Pe> peList = new ArrayList<>();

        // Ajouter une unité de traitement (PE) à la liste des PEs
        peList.add(new Pe(0, new PeProvisionerSimple(HOST_MIPS)));

        // Ajouter un hôte à la liste des hôtes
        hostList.add(
                new Host(
                        0,
                        new RamProvisionerSimple(HOST_RAM),
                        new BwProvisionerSimple(HOST_BW),
                        HOST_STORAGE,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        );

        // Caractéristiques du centre de données
        String arch = "x86"; // Architecture de la machine
        String os = "Linux"; // Système d'exploitation
        String vmm = "Xen"; // VMM (Virtual Machine Monitor) utilisé
        double time_zone = 10.0; // Fuseau horaire
        double cost = 3.0; // Coût par unité de temps
        double costPerMem = 0.05; // Coût par unité de mémoire
        double costPerStorage = 0.1; // Coût par unité de stockage
        double costPerBw = 0.1; // Coût par unité de bande passante

        // Créer les caractéristiques du centre de données
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            // Créer le centre de données avec les caractéristiques définies
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // Méthode pour créer un courtier (Broker)
    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            // Créer un courtier
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }

    // Méthode pour afficher les détails des Cloudlets terminées
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== RÉSULTATS ==========");
        Log.printLine("ID Cloudlet" + indent + "STATUT" + indent +
                "ID Data center" + indent + "ID VM" + indent + "Temps" + indent + "Temps de début" + indent + "Temps de fin");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCÈS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}







//    public static void main(String[] args) {
//        Log.printLine("Starting CloudSim simulation...");
//
//        try {
//            // Initialize CloudSim
//            int num_user = 1;
//            Calendar calendar = Calendar.getInstance();
//            boolean trace_flag = false;
//            CloudSim.init(num_user, calendar, trace_flag);
//
//            // Create Datacenters
//            Datacenter datacenter0 = createDatacenter("Datacenter_0");
//
//            // Create Broker
//            DatacenterBroker broker = createBroker();
//            int brokerId = broker.getId();
//
//            // VM and Cloudlet configurations
//            int vmMemory = 512;
//            long vmSize = 10000;
//            int vmBw = 1000;
//            int vmPesNumber = 1;
//            String vmm = "Xen";
//
//            // Create VMs
//            List<Vm> vmList = new ArrayList<>();
//            for (int i = 0; i < 5; i++) { // Start with 5 VMs
//                Vm vm = new Vm(i, brokerId, HOST_MIPS, vmPesNumber, vmMemory, vmBw, vmSize, vmm, new CloudletSchedulerTimeShared());
//                vmList.add(vm);
//            }
//
//            // Submit VM list to the broker
//            broker.submitVmList(vmList);
//
//            // Cloudlet configuration
//            int cloudletLength = 1000;
//            int cloudletPesNumber = 1;
//            long cloudletFileSize = 300;
//            long cloudletOutputSize = 300;
//            UtilizationModel utilizationModel = new UtilizationModelFull();
//
//            // Simulation scenarios
//            int[] requestsPerSecond = {200, 500, 1000, 2000};
//
//            for (int requests : requestsPerSecond) {
//                List<Cloudlet> cloudletList = new ArrayList<>();
//                for (int i = 0; i < requests; i++) {
//                    Cloudlet cloudlet = new Cloudlet(i, cloudletLength, cloudletPesNumber, cloudletFileSize, cloudletOutputSize, utilizationModel, utilizationModel, utilizationModel);
//                    cloudlet.setUserId(brokerId);
//                    cloudletList.add(cloudlet);
//                }
//
//                // Submit cloudlet list to the broker
//                broker.submitCloudletList(cloudletList);
//
//                // Start the simulation
//                CloudSim.startSimulation();
//
//                // Stop the simulation
//                CloudSim.stopSimulation();
//
//                // Print results
//                List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
//                printCloudletList(finishedCloudlets);
//
//                // Calculate average response time
//                double totalResponseTime = 0;
//                for (Cloudlet cloudlet : finishedCloudlets) {
//                    totalResponseTime += cloudlet.getFinishTime() - cloudlet.getSubmissionTime();
//                }
//                double averageResponseTime = totalResponseTime / finishedCloudlets.size();
//
//                Log.printLine("Average Response Time for " + requests + " requests/second: " + averageResponseTime + " seconds");
//            }
//
//            Log.printLine("CloudSim simulation finished!");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.printLine("The simulation has been terminated due to an unexpected error");
//        }
//    }
