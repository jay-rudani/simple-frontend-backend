let variantCounter = 1;

// Function to add a new variant form
function addVariant() {
    const variantsList = document.getElementById('variants-list');
    const newIndex = variantCounter++;

    // Template for a new variant form
    const variantTemplate = `
            <div class="variant-form" data-variant-index="${newIndex}">
                <sl-card>
                    <div class="variant-header">
                        <h4>Variant #${newIndex + 1}</h4>
                        <sl-button type="button"
                                  variant="danger"
                                  size="small"
                                  onclick="removeVariant(this)"
                                  class="remove-variant-btn">
                            <sl-icon name="trash"></sl-icon>
                        </sl-button>
                    </div>

                    <div class="variant-form-grid">
                        <sl-input name="variants[${newIndex}].title"
                                 label="Variant Title"
                                 required>
                        </sl-input>

                        <sl-input name="variants[${newIndex}].sku"
                                 label="SKU"
                                 required>
                        </sl-input>

                        <sl-input name="variants[${newIndex}].price"
                                 label="Price"
                                 type="number"
                                 min="0"
                                 step="0.01"
                                 required>
                        </sl-input>

                        <sl-input name="variants[${newIndex}].option1"
                                 label="Option 1">
                        </sl-input>

                        <sl-input name="variants[${newIndex}].option2"
                                 label="Option 2">
                        </sl-input>

                       <div class="switch-box">
                        <label>Available</label>
                        <sl-switch
                            name="variants[${newIndex}].available"
                            label="Available"
                            value="true">
                        </sl-switch>
                       </div>
                    </div>
                </sl-card>
            </div>
        `;

    // Add the new variant form to the list
    variantsList.insertAdjacentHTML('beforeend', variantTemplate);
}

// Function to remove a variant form
function removeVariant(button) {
    const variantForm = button.closest('.variant-form');
    const variantsList = document.getElementById('variants-list');

    // Don't remove if it's the last variant
    if (variantsList.children.length > 1) {
        variantForm.remove();
        updateVariantNumbers();
    } else {

        // Show error message if there is only one variant
        const alert = Object.assign(document.createElement('sl-alert'), {
            variant: 'danger',
            closable: true,
            duration: 3000,
            innerHTML: `
                    <sl-icon slot="icon" name="exclamation-triangle"></sl-icon>
                    At least one variant is required
                `
        });

        document.body.append(alert);
        alert.toast();
    }
}

// Function to update the variant numbers after adding or removing a variant
function updateVariantNumbers() {
    const variants = document.querySelectorAll('.variant-form');
    variants.forEach((variant, index) => {
        variant.querySelector('h4').textContent = `Variant #${index + 1}`;
    });
}

// Clear form after successful submission and reset variant counter
document.body.addEventListener('htmx:afterSwap', function (evt) {
    if (evt.detail.target.id === 'products-table') {
        document.getElementById('productForm').reset();

        // Reset variants to just one
        const variantsList = document.getElementById('variants-list');
        while (variantsList.children.length > 1) {
            variantsList.lastElementChild.remove();
        }
        variantCounter = 1;
        updateVariantNumbers();
    }
});