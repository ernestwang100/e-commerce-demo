terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "4.51.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

variable "project_id" {
  description = "The ID of the GCP project"
  type        = string
}

variable "region" {
  description = "GCP Region"
  default     = "us-central1"
}

variable "zone" {
  description = "GCP Zone"
  default     = "us-central1-a"
}

variable "machine_type" {
  description = "Machine type for the VM"
  default     = "e2-medium"
}

resource "google_compute_network" "vpc_network" {
  name = "shopping-network"
}

resource "google_compute_firewall" "allow_web" {
  name    = "allow-web"
  network = google_compute_network.vpc_network.name

  allow {
    protocol = "tcp"
    ports    = ["80", "443", "4200", "7070"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["web-server"]
}

resource "google_compute_firewall" "allow_ssh" {
  name    = "allow-ssh"
  network = google_compute_network.vpc_network.name

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["0.0.0.0/0"] # Consider restricting this to your specific IP for security
  target_tags   = ["ssh-server"]
}

resource "google_compute_instance" "vm_instance" {
  name         = "shopping-vm"
  machine_type = var.machine_type
  tags         = ["web-server", "ssh-server"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
      size  = 20 # 20GB size
    }
  }

  network_interface {
    network = google_compute_network.vpc_network.name
    access_config {
      # Ephemeral public IP
    }
  }

  metadata_startup_script = file("startup.sh")

  # Setup SSH keys - Replace 'username' and public key path as needed or rely on OS Login
  # metadata = {
  #   ssh-keys = "username:${file("~/.ssh/id_rsa.pub")}"
  # }
}

output "ip" {
  value = google_compute_instance.vm_instance.network_interface.0.access_config.0.nat_ip
}
