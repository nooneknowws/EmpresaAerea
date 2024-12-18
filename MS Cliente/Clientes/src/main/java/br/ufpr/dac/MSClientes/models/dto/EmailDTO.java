package br.ufpr.dac.MSClientes.models.dto;

public class EmailDTO {
	
	public EmailDTO(String email, boolean available, String message) {
		super();
		this.email = email;
		this.available = available;
		this.message = message;
	}
	
	private final String email;
    private final boolean available;
    private final String message;
    
    
	public String getEmail() {
		return email;
	}
	public boolean isAvailable() {
		return available;
	}
	public String getMessage() {
		return message;
	}
    
}