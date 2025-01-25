# Temporal-Tie-Strength Group-Based Routing Scheme (TS-GBR)

This repository contains the code for the **TS-GBR** (Temporal-Tie-Strength Group-Based Routing Scheme) designed to improve the cost-effectiveness of message delivery 
in mobile opportunistic networks.

## Description

The TS-GBR scheme exploits several contact properties such as contact consistency, frequency, duration time, and contact aggregate to form k groups of nodes that
exhibit strong contact relationships. The temporal-tie strength metric is introduced to interpret intra- and inter-group contact patterns along with a time window. 
The scheme shows significant improvements in delivery rate and overhead when applied to SLAW, TVCM, and NCCU trace data.

## Installation

1. Refer to the following link for installing The ONE simulator
    https://github.com/akeranen/the-one


## Overview of Files
1:- **GBR_ContactTimesReport**:
    Maps contact times between hosts to integer intervals (e.g., hourly).
    Stores pairwise contact times and provides structured temporal data for analysis.

2:-**GBR_GroupFormationProcessA**:
    Extracts groups from KClique_lists formed in daily intervals (T_i).
    Divided into two sub-processes: GBR_getMaxKClique and GBR_makeKclique.

3:-**GBR_GroupFormationProcessB**:
    Finalizes group formation based on GBR conditions.
    Processes daily k-clique data and returns final groups for simulation.

4:-**GBR_Kclique**:
    Implements k-clique detection in temporal graphs.
    Processes contact data to identify cliques and monitor host connections.

5:-**GBR_MeanDuration**:
    Calculates the mean duration of contacts between hosts.
    Provides statistical insights into inter-contact durations in DTN simulations.

6:-**GBR_MeanICT**:
    Generates encounter reports for GBR.
    Useful for analyzing inter-contact times and other metrics.

7:-**GBRouter**:
    Implements the GBR-based routing protocol. Designed to facilitate efficient data transfer in DTNs

8:-**GroupSelector**
    This class is responsible for displaying the final groups formed after filtering out overlapping elements.
    
9:- **GroupDurationCondition**
     This class applies duration condition to filter groups further. 
     It removes members from the group if the average duration of their connections does not meet the specified threshold (beta).
10:- **CalGrouprate**
`    The CalGrouprate class is designed to manage and calculate group rates using a nested mapping structure.



