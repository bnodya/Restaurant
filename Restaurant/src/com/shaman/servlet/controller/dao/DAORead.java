package com.shaman.servlet.controller.dao;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.shaman.servlet.controller.connection.ConnectionManager;
import com.shaman.servlet.controller.connection.Query;
import com.shaman.servlet.controller.dao.daoenum.QueryType;
import com.shaman.servlet.controller.dao.daoenum.TableName;
import com.shaman.servlet.controller.transformer.Transformer;
import com.shaman.servlet.model.Item;

public class DAORead {

	DAOFactory fac = null;
	static Connection con = null;

	public DAORead(DAOFactory factory) {
		fac = factory;
		initialize();
	}

	private void initialize() {
		try {
			con = ConnectionManager.getInstance().getConnection();
		} catch (SQLException | IOException | PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getAll(TableName tableName) throws SQLException {
		Transformer<T> transformer = new Transformer<T>(
				tableName.getClassType());
		List<T> list = new ArrayList<T>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select * from " + tableName);
			rs = ps.executeQuery();
			list = transformer.getPojoList(rs);
		} finally {
			DAOFactory.closeAll(ps, rs);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getAllForInput(TableName tableName, String columnName,
			String searchValue) throws SQLException {
		Transformer<T> transformer = new Transformer<T>(
				tableName.getClassType());
		List<T> list = new ArrayList<T>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM " + tableName + " WHERE "
					+ columnName + " LIKE '%" + searchValue + "%'");
			rs = ps.executeQuery();
			list = transformer.getPojoList(rs);
		} finally {
			DAOFactory.closeAll(ps, rs);
		}
		return list;
	}

	public <T> T getPojoForPrimarKey(TableName tableName, String primaryKey)
			throws SQLException {
		@SuppressWarnings("unchecked")
		Transformer<T> transformer = new Transformer<T>(
				tableName.getClassType());
		T currentPojo = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String queryString = Specific.queryString(tableName, primaryKey,
					QueryType.READ);
			ps = con.prepareStatement(queryString);
			rs = ps.executeQuery();
			if (rs.next()) {
				currentPojo = transformer.getPojo(rs);
			}
		} finally {
			DAOFactory.closeAll(ps, rs);
		}
		return currentPojo;
	}

	public <T> boolean alreadyExisting(TableName tableName, String primaryKey)
			throws SQLException {
		if (getPojoForPrimarKey(tableName, primaryKey) != null) {
			return true;
		} else {
			return false;
		}
	}

	public <T> boolean alreadyExisting(TableName tableName, T currentPojo)
			throws SQLException {
		String primaryKey = Specific.<T> getPrimaryKey(tableName, currentPojo);
		if (alreadyExisting(tableName, primaryKey) == false) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isRegistered(String login, String password) {

		boolean status = false;
		try {
			con = ConnectionManager.getInstance().getConnection();
			PreparedStatement stmt = con
					.prepareStatement(Query.SELECT_USER_BY_NAME_AND_PASSWORD);
			stmt.setString(1, login);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				status = true;
			}
		} catch (SQLException | IOException | PropertyVetoException e) {
			e.printStackTrace();
		}
		return status;
	}

	@SuppressWarnings("unchecked")
	public List<Item> getItemsBy(TableName tableName, int id){
		ResultSet rs = null;
		List<Item> list = null;
		Transformer<Item> transformer = new Transformer<Item>(
				tableName.getClassType());
		try {
			con = ConnectionManager.getInstance().getConnection();
			PreparedStatement stmt = null;
			switch(tableName){
			case USER:
				stmt = con.prepareStatement(Query.SELECT_ITEMS_BY_USER_ID);
				stmt.setInt(1, id);
				rs = stmt.executeQuery();
				break;
			case ORDER:
				stmt = con.prepareStatement(Query.SELECT_ITEMS_BY_ORDER_ID);
				stmt.setInt(1, id);
				rs = stmt.executeQuery();
				break;
			default:
				break;	
			}
			
			if (rs.next()) {
				list = transformer.getPojoList(rs);
			}
		} catch (SQLException | IOException | PropertyVetoException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static double getOrderPriceByUserId(int id){
		double sum = 0;
		ResultSet rs = null;
		try {
			PreparedStatement ps = con.prepareStatement(Query.SELECT_PRICE_AND_QUANTITY_BY_USER_ID);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			sum = Transformer.resultSetToPrice(rs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		return sum;
	}
}
