package com.OMT.Backend.config;

import com.OMT.Backend.model.*;
import com.OMT.Backend.repository.UserRepository;
import com.OMT.Backend.repository.VehicleRepository;
import com.OMT.Backend.repository.YardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final YardRepository yardRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      YardRepository yardRepository,
                      VehicleRepository vehicleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.yardRepository = yardRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedYardsAndVehicles();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@omt.com")) {
            User admin = User.builder()
                    .name("OMT Administrator")
                    .email("admin@omt.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("staff@omt.com")) {
            User staff = User.builder()
                    .name("OMT Yard Operations Staff")
                    .email("staff@omt.com")
                    .passwordHash(passwordEncoder.encode("Staff@123"))
                    .role(Role.STAFF)
                    .active(true)
                    .build();
            userRepository.save(staff);
        }
    }

    private void seedYardsAndVehicles() {
        if (yardRepository.count() == 0) {
            Yard yard1 = Yard.builder()
                    .yardCode("YRD-0001")
                    .name("Rotterdam Central Yard")
                    .country("Netherlands")
                    .city("Rotterdam")
                    .address("Havenbedrijf 42, Maasvlakte")
                    .status(YardStatus.ACTIVE)
                    .build();

            Yard yard2 = Yard.builder()
                    .yardCode("YRD-0002")
                    .name("Antwerp Dismantling Center")
                    .country("Belgium")
                    .city("Antwerp")
                    .address("Noorderlaan 108, Haven 200")
                    .status(YardStatus.ACTIVE)
                    .build();

            Yard yard3 = Yard.builder()
                    .yardCode("YRD-0003")
                    .name("Duisburg Inland Terminal")
                    .country("Germany")
                    .city("Duisburg")
                    .address("Ruhrort Hafenstrasse 15")
                    .status(YardStatus.ACTIVE)
                    .build();

            Yard yard4 = Yard.builder()
                    .yardCode("YRD-0004")
                    .name("Hamburg South Storage (Archived)")
                    .country("Germany")
                    .city("Hamburg")
                    .address("Waltershofer Damm 7")
                    .status(YardStatus.INACTIVE)
                    .build();

            yardRepository.saveAll(List.of(yard1, yard2, yard3, yard4));

            // Seed Vehicles
            Vehicle v1 = Vehicle.builder()
                    .assetCode("AST-0001")
                    .vin("1HGCR2F83HA001924")
                    .make("Honda")
                    .model("Accord EX-L")
                    .manufactureYear(2017)
                    .yard(yard1)
                    .routingDecision(RoutingDecision.DISMANTLE)
                    .status(VehicleStatus.RECEIVED)
                    .notes("Front axle damage reported during gate inspection; eligible for parts harvesting.")
                    .build();

            Vehicle v2 = Vehicle.builder()
                    .assetCode("AST-0002")
                    .vin("WAUZZZ8V7FA018492")
                    .make("Audi")
                    .model("A3 Sportback")
                    .manufactureYear(2020)
                    .yard(yard1)
                    .routingDecision(RoutingDecision.KEEP_WHOLE)
                    .status(VehicleStatus.IN_PROGRESS)
                    .notes("Undergoing secondary evaluation and cosmetic repair checklist.")
                    .build();

            Vehicle v3 = Vehicle.builder()
                    .assetCode("AST-0003")
                    .vin("3VW2K7AJ4EM190284")
                    .make("Volkswagen")
                    .model("Golf 1.4 TSI")
                    .manufactureYear(2019)
                    .yard(yard2)
                    .routingDecision(RoutingDecision.DISMANTLE)
                    .status(VehicleStatus.COMPLETED)
                    .notes("Catalytic converter and transmission salvage complete. Vehicle archived.")
                    .build();

            Vehicle v4 = Vehicle.builder()
                    .assetCode("AST-0004")
                    .vin("5UXCR6C05L9B83719")
                    .make("BMW")
                    .model("X5 xDrive40i")
                    .manufactureYear(2021)
                    .yard(yard2)
                    .routingDecision(RoutingDecision.KEEP_WHOLE)
                    .status(VehicleStatus.RECEIVED)
                    .notes("Key present, runs and drives smoothly. Awaiting valuation.")
                    .build();

            Vehicle v5 = Vehicle.builder()
                    .assetCode("AST-0005")
                    .vin("JM1BM1V76M1928471")
                    .make("Mazda")
                    .model("CX-5 Touring")
                    .manufactureYear(2022)
                    .yard(yard3)
                    .routingDecision(RoutingDecision.KEEP_WHOLE)
                    .status(VehicleStatus.IN_PROGRESS)
                    .notes("Awaiting title clearance and documentation check.")
                    .build();

            vehicleRepository.saveAll(List.of(v1, v2, v3, v4, v5));
        }
    }
}

