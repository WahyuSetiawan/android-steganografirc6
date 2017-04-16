/**
 * Copyright (C) 2009  Pasquale Paola

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package siapasaja.steganografirc6.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * @author <a href="mailto:pasquale.paola@gmail.com">Pasquale Paola</a>
 *
 */
public class MmsIntent {
	
	
	private Uri uri;	
	private final Context context;

	public Uri getUri() {
		return uri;
	}


	public void setUri(Uri uri) {
		this.uri = uri;
	}	

	
	
	/**
	 * Create a template to send MMS with encoded image.
	 * @param uri Uri of the encoded image.
	 * @param context Handler to start Activity.
	 */
	public MmsIntent(Uri uri,Context context) {		
		this.uri = uri;
		this.context=context;
	}


	/**
	 * Invoke default android editor MMS, with encoded image attached.
	 */
	public void send()
	{
		Intent sendIntent = new Intent(Intent.ACTION_SEND); 
		sendIntent.putExtra("sms_body", ""); 
		sendIntent.putExtra(Intent.EXTRA_STREAM,uri);
		sendIntent.setType("image/png"); 
		context.startActivity(sendIntent);
	}

}
