# 🔧 Self-Healing Infrastructure Demo

A comprehensive demonstration of self-healing infrastructure capabilities using Event-Driven Ansible (EDA). This project simulates cell tower infrastructure that automatically detects configuration drift and remediates failures without manual intervention.

## 📋 Overview

This demo showcases:

- **Automatic Drift Detection**: Monitors cell tower configurations and detects when they deviate from the desired state
- **Event-Driven Remediation**: Uses EDA rulebooks to automatically trigger remediation workflows when issues are detected
- **Infrastructure Simulation**: Simulates multiple cell towers with configurable drop rates to demonstrate real-world scenarios
- **Integration with ServiceNow**: Creates incidents for tracking and approval workflows
- **Real-time Monitoring**: Provides dashboards and Kafka UI for monitoring system health

## 🎯 Architecture

The demo consists of:

1. **Cell Tower Simulators**: Java/Quarkus applications that simulate cell tower behavior and emit call records
2. **Kafka**: Message broker for streaming call records and alerts
3. **Apache Flink**: Stream processing for analyzing call data and detecting anomalies
4. **Ansible Automation Platform**: Orchestrates remediation workflows
5. **Event-Driven Ansible**: Listens for events and triggers automated responses
6. **Network Dashboard**: Visualizes tower health and metrics

## 📦 Prerequisites

Before installing this demo, ensure you have:

1. **A running OpenShift Container Platform (OCP) cluster**
   - Access to the cluster with admin privileges
   - Sufficient resources for deploying AAP and supporting infrastructure

2. **Ansible Automation Platform subscription**
   - Valid Red Hat subscription with AAP entitlement
   - Access to the Red Hat Operator Catalog

3. **A ServiceNow instance**
   - Access to a ServiceNow instance for incident creation and tracking

4. **OpenShift CLI (`oc`) installed and configured**

5. **Ansible installed locally** (for running the setup playbook)

## 🚀 Installation

### Step 1: Login to OpenShift

First, authenticate to your OpenShift cluster:

```bash
oc login -u <admin_username> -p <admin_password> <api_url>
```

### Step 2: Configure Secrets

Before running the setup playbook, you need to configure your secrets file. Copy the template and fill in your values:

```bash
cp vars/your_secrets.yml vars/secrets.yml
```

Edit `vars/secrets.yml` and fill in the following values:

- **`ocp_api_host`**: Your OpenShift API URL (e.g., `https://api.your-cluster.example.com:6443`)
- **`ocp_bearer_token`**: Your OpenShift bearer token (get it with `oc whoami -t` or create a service account token)
- **`supabase_anon_key`**: Your Supabase anonymous key (for event tracking)
- **`servicenow_host`**: Your ServiceNow instance hostname (e.g., `dev12345.service-now.com`)
- **`servicenow_username`**: Your ServiceNow username
- **`servicenow_password`**: Your ServiceNow password
- **`cell_id`**: Default cell tower ID (e.g., `ATX`, `NYC`, `CHI`)

**Note**: The `vars/secrets.yml` file is gitignored and should not be committed to version control.

### Step 3: Run the Setup Playbook

Navigate to the `tower-setup` directory and run the main setup playbook with your secrets file:

```bash
cd tower-setup
ansible-playbook setup-all.yml -e @../vars/secrets.yml
```

This playbook will:

1. **Deploy Infrastructure Components**:
   - Kafka cluster and topics
   - Kafka UI for monitoring
   - Apache Flink for stream processing
   - Cell tower simulators
   - Network dashboard

2. **Install and Configure AAP**:
   - Install the AAP Operator
   - Deploy Ansible Automation Platform instance
   - Create projects, credentials, and job templates
   - Set up workflow templates for remediation

3. **Configure Event-Driven Ansible**:
   - Create EDA projects and rulebooks
   - Set up rulebook activations for automatic remediation
   - Configure event sources (Kafka listeners)

### Step 4: Verify Installation

After the playbook completes, verify the installation:

1. **Check AAP Routes**:
   ```bash
   oc get routes -n aap
   ```
   You should see routes for:
   - AAP (Automation Platform)
   - AAP Controller
   - AAP EDA

2. **Check Cell Tower Simulators**:
   ```bash
   oc get pods -n openshift-operators | grep tower
   ```

3. **Check Kafka and Flink**:
   ```bash
   oc get pods -n openshift-operators | grep -E "kafka|flink"
   ```

## 🔄 How It Works

### 1. Monitoring and Detection

- Cell tower simulators continuously emit call records to Kafka
- Flink processes these records and calculates drop rates
- When a tower's drop rate exceeds the threshold (3%), an alert is published to Kafka

### 2. Event-Driven Response

- EDA rulebook listens to the `dropped-alerts` Kafka topic
- When an alert is received, it triggers the remediation workflow in AAP

### 3. Remediation Workflow

The remediation workflow (`cell-tower-remediation-workflow`) performs:

1. **Check Tower State**: Runs a check mode playbook to detect configuration drift
2. **Detect Drift**: Analyzes the check results to identify drifted configurations
3. **Create ServiceNow Incident**: Creates a ticket for tracking (if drift detected)
4. **Approval Gate**: Requires manual approval before remediation
5. **Remediate**: Applies the correct configuration to restore the tower to desired state

### 4. Configuration as Code

Tower configurations are stored in Git (`vars/cell_towers.yml`), serving as the source of truth. The remediation playbook ensures towers match this desired state.

## 📁 Project Structure

```
.
├── playbooks/
│   ├── detect-drift.yml          # Detects configuration drift from check mode results
│   ├── setup-cell-tower.yml      # Sets up/remediates cell tower configurations
│   └── create-servicenow-incident.yml  # Creates ServiceNow tickets
├── rulebooks/
│   └── rulebook.yml               # EDA rulebook for automatic remediation triggers
├── tower-setup/
│   ├── setup-all.yml              # Main setup playbook
│   ├── playbook.yml               # Infrastructure deployment
│   ├── aap.yml                    # AAP installation and configuration
│   └── tasks/                     # Task files for various components
├── simulator/                     # Cell tower simulator application
└── vars/
    ├── cell_towers.yml            # Source of truth for tower configurations
    ├── your_secrets.yml           # Template for secrets (copy to secrets.yml)
    └── secrets.yml                 # Secret variables (gitignored, not in repo)
```

## 🎮 Usage

### Accessing AAP

After installation, access the AAP Controller:

1. Get the route:
   ```bash
   oc get route aap-controller -n aap -o jsonpath='{.spec.host}'
   ```

2. Get the admin password:
   ```bash
   oc get secret aap-controller-admin-password -n aap -o jsonpath='{.data.password}' | base64 -d
   ```

3. Login at: `https://<route-hostname>`

### Triggering Manual Remediation

You can manually trigger the remediation workflow:

1. Navigate to **Templates** → **Workflow Templates** in AAP
2. Select `cell-tower-remediation-workflow`
3. Click **Launch**
4. Provide the `cell_id` variable (e.g., `NYC`, `CHI`, `ATX`)

### Simulating Tower Failures

To test the self-healing capabilities:

1. Access a cell tower simulator route
2. Increase the drop rate:
   ```bash
   curl -X POST http://<tower-route>/control/drop-rate?value=0.5
   ```
3. Watch EDA detect the issue and trigger remediation

## 🛠️ Customization

### Adding New Towers

Edit `vars/cell_towers.yml` to add new cell towers:

```yaml
cell_towers:
  - name: new_city
    deployment_name: new-city-tower-simulator
    app_label: new-city-tower-simulator
    city_code: "NEW"
    max_active_calls: 400
```

Then re-run the setup playbook or the `setup-cell-tower` job template.

### Adjusting Thresholds

Modify the drift detection threshold in `rulebooks/rulebook.yml`:

```yaml
condition: event.body.dropRate > 0.05  # Change from 0.03 to 0.05
```

## 📝 Notes

- The setup process may take 15-30 minutes depending on cluster resources
- Ensure you have sufficient cluster capacity for AAP (recommended: 8+ CPU cores, 32GB+ RAM)
- ServiceNow integration requires valid credentials configured in `vars/secrets.yml`
- The demo uses Supabase for event tracking (configure credentials in `vars/secrets.yml`)
- **Important**: Make sure to configure `vars/secrets.yml` with your actual values before running the setup playbook

## 📄 License

MIT License. Use it, break it, improve it.
