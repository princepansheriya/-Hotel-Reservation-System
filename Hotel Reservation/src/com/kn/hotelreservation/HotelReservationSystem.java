package com.kn.hotelreservation;

import java.sql.*;
import java.util.Scanner;

public class HotelReservationSystem {

	// Database connection information
	private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
	private static final String username = "root";
	private static final String password = "asdfg";

	public static void main(String[] args) throws Exception {

		try {
			// Register the MySQL JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver");

		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			// Establish a connection to the MySQL database

			Connection connection = DriverManager.getConnection(url, username, password);
			Statement statment = connection.createStatement();

			// Display the main menu for the Hotel Management System
			while (true) {
				System.out.println();
				System.out.println("HOTEL MANAGEMENT SYSTEM");
				System.out.println("1. Reserve a room");
				System.out.println("2. view Reservations");
				System.out.println("3. Get Room Number");
				System.out.println("4. Update Reservations");
				System.out.println("5. Delete Reservations");
				System.out.println("0. Exit");
				System.out.print("Choose an option:  ");
				Scanner scan = new Scanner(System.in);
				int choice = scan.nextInt();

				// Read the user's choice
				switch (choice) {
				case 1:
					reservRoom(connection, scan);
					break;
				case 2:
					viewReservations(connection, statment, scan);
					break;
				case 3:
					getRoomNumber(connection, statment, scan);
					break;
				case 4:
					updateReservation(connection, statment, scan);
					break;
				case 5:
					deleteReservation(connection, statment, scan);
					break;
				case 0:
					exit();
					scan.close();
					return;
				default:
					System.out.println("Invalid choice. Try again.");
				}

			}

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private static void updateReservation(Connection connection, Statement statement, Scanner scan) {
		try {
			System.out.print("Enter reservation ID to update: ");
			int reservationId = scan.nextInt();
			scan.nextLine(); // Consume the newline character

			if (!reservationExists(connection, reservationId)) {
				System.out.println("Reservation not found for the given ID.");
				return;
			}

			System.out.print("Enter new room number: ");
			int newRoomNumber = scan.nextInt();
			scan.nextLine();

			if (isRoomAlreadyBooked(connection, newRoomNumber)) {
				System.out.println("Room " + newRoomNumber + " is already booked. Please choose another room.");
				return; // Return to the main menu
			}

			System.out.print("Enter new mobile number: ");
			String newMobileNumber = scan.next();
			String sql = "UPDATE reservations SET room_number = ?, contact_number = ? WHERE reservation_id = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, newRoomNumber);
			preparedStatement.setString(2, newMobileNumber);
			preparedStatement.setInt(3, reservationId);

			int affectedRows = preparedStatement.executeUpdate();

			if (affectedRows > 0) {
				System.out.println("Reservation updated successfully!");
			} else {
				System.out.println("Reservation update failed.");
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// Function to check if a reservation with the given ID exists
	private static boolean reservationExists(Connection connection, int reservationId) {
		try {
			String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);

			return resultSet.next();

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}

	}

	// Function to reserve a room
	private static void reservRoom(Connection connection, Scanner scan) {
		try {
			System.out.println("Enter guest name: ");
			String guestName = scan.next();
			System.out.println("Enter room number: ");
			int roomNumber = scan.nextInt();

			// Check if the room is already booked
			if (isRoomAlreadyBooked(connection, roomNumber)) {
				System.out.println("Room " + roomNumber + " is already booked. Please choose another room.");
				return; // Return to the main menu
			}

			scan.nextLine(); // Consume the newline character

			System.out.println("Enter contact number: ");
			String contactNumber = scan.next();

			// SQL query to insert reservation data into the database
			String sql = "INSERT INTO reservations (guest_name, room_number, contact_number) VALUES (?, ?, ?)";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, guestName);
			preparedStatement.setInt(2, roomNumber);
			preparedStatement.setString(3, contactNumber);

			int affectedRows = preparedStatement.executeUpdate();

			if (affectedRows > 0) {
				System.out.println("Reservation successful!");
			} else {
				System.out.println("Reservation failed.");
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// Function to check if a room is already booked
	private static boolean isRoomAlreadyBooked(Connection connection, int roomNumber) {
		try {
			String sql = "SELECT reservation_id FROM reservations WHERE room_number = " + roomNumber;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);

			return resultSet.next();
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	// Function to view reservations
	private static void viewReservations(Connection connection, Statement statement, Scanner scan) {
		try {
			String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";

			ResultSet resultSet = statement.executeQuery(sql);

			System.out.println("Current Reservations:");
			System.out.println(
					"+----------------+-----------------+---------------+----------------------+-------------------------+");
			System.out.println(
					"| Reservation ID | Guest           | Room Number   | Contact Number      | Reservation Date        |");
			System.out.println(
					"+----------------+-----------------+---------------+----------------------+-------------------------+");

			while (resultSet.next()) {
				int reservationId = resultSet.getInt("reservation_id");
				String guestName = resultSet.getString("guest_name");
				int roomNumber = resultSet.getInt("room_number");
				String contactNumber = resultSet.getString("contact_number");
				String reservationDate = resultSet.getTimestamp("reservation_date").toString();

				System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s   |\n", reservationId, guestName, roomNumber,
						contactNumber, reservationDate);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// Function to retrieve the room number for a specific reservation
	private static void getRoomNumber(Connection connection, Statement statment, Scanner scan) {
		try {
			System.out.print("Enter reservation ID: ");
			int reservationId = scan.nextInt();
			System.out.print("Enter guest name: ");
			String guestName = scan.next();

			String sql = "SELECT room_number FROM reservations " + "WHERE reservation_id = " + reservationId
					+ " AND guest_name = '" + guestName + "'";

			ResultSet resultSet = statment.executeQuery(sql);

			if (resultSet.next()) {
				int roomNumber = resultSet.getInt("room_number");
				System.out.println("Room number for Reservation ID " + reservationId + " and Guest " + guestName
						+ " is: " + roomNumber);
			} else {
				System.out.println("Reservation not found for the given ID and guest name.");
			}

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	// The function to delete a reservation
	private static void deleteReservation(Connection connection, Statement statment, Scanner scan) {
		try {
			System.out.print("Enter reservation ID to delete: ");
			int reservationId = scan.nextInt();

			if (!reservationExists(connection, reservationId)) {
				System.out.println("Reservation not found for the given ID.");
				return;
			}

			// Delete the reservation from the database
			String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;
			int affectedRows = statment.executeUpdate(sql);

			if (affectedRows > 0) {
				System.out.println("Reservation deleted successfully!");
			} else {
				System.out.println("Reservation deletion failed.");
			}
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	// The "exit" function to gracefully terminate the program
	private static void exit() throws InterruptedException {
		System.out.print("Exiting System");
		int i = 5;
		while (i != 0) {
			System.out.print(".");
			Thread.sleep(1000);
			i--;
		}
		System.out.println();
		System.out.println("ThankYou For Using Hotel Reservation System!!!");

	}

}
