/**
 * This file is part of TKConfig application.
 * 
 * Copyright (C) 2013 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.tkconfig.activities;

import ro.ciubex.tkconfig.R;
import ro.ciubex.tkconfig.dialogs.GpsContactEditor;
import ro.ciubex.tkconfig.list.GpsContactListAdapter;
import ro.ciubex.tkconfig.models.GpsContact;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * This activity show to the user a list of available GPS phones.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class GpsContactActivity extends BaseActivity {
	private GpsContactListAdapter adapter;
	private ListView contactList;
	private final int CONFIRM_ID_DELETE = 0;

	/**
	 * The method invoked when the activity is creating
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_contact_list_layout);
		setMenuId(R.menu.gps_contact_menu);
		prepareGpsContactListView();
	}

	/**
	 * Method invoked when the activity is started.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		app.showProgressDialog(this, R.string.please_wait);
		app.historiesLoad();
		reloadAdapter();
	}
	
	/**
	 * Invoked when the activity is put on pause
	 */
	@Override
	protected void onPause() {
		if (adapter.isModified()) {
			app.contactsSave();
			adapter.setModified(false);
		}
		super.onPause();
	}

	/**
	 * Method used to initialize the history list view.
	 */
	private void prepareGpsContactListView() {
		contactList = (ListView) findViewById(R.id.gps_contact_list);
		contactList.setEmptyView(findViewById(R.id.no_contacts));
		contactList.setItemsCanFocus(false);
		contactList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position > -1 && position < adapter.getCount()) {
					showItemDialogMenu(position);
				}
			}
		});
		adapter = new GpsContactListAdapter(app);
		contactList.setAdapter(adapter);
	}

	/**
	 * Reload adapter and histories list.
	 */
	public void reloadAdapter() {
		adapter.notifyDataSetChanged();
		contactList.invalidateViews();
		contactList.scrollBy(0, 0);
		contactList.setFastScrollEnabled(app.getContacts().size() > 50);
		app.hideProgressDialog();
	}

	/**
	 * This method show the pop up menu when the user do a long click on a list
	 * item.
	 * 
	 * @param contactPosition
	 *            The contact position where was made the long click
	 */
	private void showItemDialogMenu(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.item_edit);
		builder.setItems(R.array.contacts_menu_list,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							onMenuItemEdit(position);
							break;
						case 1:
							onMenuItemAdd();
							break;
						case 2:
							onMenuItemDelete(position);
							break;
						}
					}
				});
		builder.create().show();
	}

	/**
	 * Prepare Option menu
	 */
	@Override
	protected boolean onMenuItemSelected(int menuItemId) {
		boolean processed = false;
		switch (menuItemId) {
		case R.id.menu_back:
			processed = true;
			goBack();
			break;
		case R.id.menu_add:
			processed = true;
			onMenuItemAdd();
			break;
		}
		return processed;
	}

	/**
	 * This method is invoked when the user chose to edit a contact item.
	 * 
	 * @param position
	 *            The position of contact item to be edited.
	 */
	private void onMenuItemEdit(int position) {
		GpsContact contact = (GpsContact) adapter.getItem(position);
		new GpsContactEditor(this, app, R.string.contact_editor_edit, contact)
				.show();
	}

	/**
	 * This method is invoked when the user chose to add a new contact item.
	 */
	private void onMenuItemAdd() {
		new GpsContactEditor(this, app, R.string.contact_editor_add, null).show();
	}

	/**
	 * This method is invoked when the user chose to delete a contact item.
	 * 
	 * @param position
	 *            The position of contact item to be deleted.
	 */
	private void onMenuItemDelete(int position) {
		final GpsContact contact = (GpsContact) adapter.getItem(position);
		if (contact != null) {
			showConfirmationDialog(
					R.string.remove_history,
					app.getString(R.string.remove_gps_contact_question,
							contact.getName()), CONFIRM_ID_DELETE, contact);
		}
	}

	/**
	 * This method is invoked by the each time when is accepted a confirmation
	 * dialog.
	 * 
	 * @param positive
	 *            True if the confirmation is positive.
	 * @param confirmationId
	 *            The confirmation ID to identify the case.
	 * @param anObject
	 *            An object send by the caller method.
	 */
	@Override
	protected void onConfirmation(boolean positive, int confirmationId,
			Object anObject) {
		if (positive) {
			switch (confirmationId) {
			case CONFIRM_ID_DELETE:
				doDeleteContact((GpsContact) anObject);
				break;
			}
		}
	}

	/**
	 * Delete the provided contact from the list.
	 * 
	 * @param contact
	 *            The contact to be deleted.
	 */
	private void doDeleteContact(GpsContact contact) {
		app.showProgressDialog(this, R.string.please_wait);
		app.getContacts().remove(contact);
		app.contactsSave();
		reloadAdapter();
	}
}